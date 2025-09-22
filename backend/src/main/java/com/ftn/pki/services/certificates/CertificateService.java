package com.ftn.pki.services.certificates;

import com.ftn.pki.dtos.certificates.CreateCertificateDTO;
import com.ftn.pki.dtos.certificates.SimpleCertificateDTO;
import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.models.certificates.Issuer;
import com.ftn.pki.models.certificates.Subject;
import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.models.users.User;
import com.ftn.pki.repositories.certificates.CertificateRepository;
import com.ftn.pki.services.users.UserService;
import com.ftn.pki.utils.certificates.CertificateUtils;
import com.ftn.pki.utils.cryptography.AESUtils;
import com.ftn.pki.utils.cryptography.RSAUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateUtils certificateUtils;
    private final AESUtils aesUtils;
    private final RSAUtils rsaUtils;
    private final UserService userService;
    private final SecretKey masterKey;

    @Autowired
    public CertificateService(CertificateRepository certificateRepository,
                              CertificateUtils certificateUtils,
                              AESUtils aesUtils, UserService userService,
                              RSAUtils rsaUtils,
                              @Value("${MASTER_KEY}") String base64MasterKey) {
        this.certificateRepository = certificateRepository;
        this.certificateUtils = certificateUtils;
        this.aesUtils = aesUtils;
        this.userService = userService;
        this.rsaUtils = rsaUtils;
        this.masterKey = AESUtils.secretKeyFromBase64(base64MasterKey);
    }

    public Certificate createCertificate(CreateCertificateDTO dto) throws Exception {
        User currentUser = userService.getLoggedUser();
        Organization organization = currentUser.getOrganization();
        if (organization == null) {
            throw new IllegalArgumentException("User does not belong to any organization");
        }

        // --- 2. Generate Subject key pair ---
        KeyPair subjectKeyPair = RSAUtils.generateRSAKeyPair();
        PublicKey subjectPublicKey = subjectKeyPair.getPublic();
        PrivateKey subjectPrivateKey = subjectKeyPair.getPrivate();

        // --- 3. Generate X500Name for Subject ---
        X500Name subjectX500 = new X500NameBuilder()
                .addRDN(BCStyle.CN, dto.getCommonName())
                .addRDN(BCStyle.SURNAME, dto.getSurname())
                .addRDN(BCStyle.GIVENNAME, dto.getGivenName())
                .addRDN(BCStyle.O, dto.getOrganization())
                .addRDN(BCStyle.OU, dto.getOrganizationalUnit())
                .addRDN(BCStyle.C, dto.getCountry())
                .addRDN(BCStyle.E, dto.getEmail())
                .build();

        Subject subject = new Subject(subjectPublicKey, subjectX500);

        // --- 4. Fetch Issuer ---
        Issuer issuer;
        Certificate issuerCertEntity = null;
        if (dto.getIssuerCertificateId() == null && dto.getType() == CertificateType.ROOT) {
            // Self-signed Root
            issuer = new Issuer(subjectPrivateKey, subjectX500);
        } else {
            if (dto.getIssuerCertificateId() == null) {
                throw new IllegalArgumentException("Issuer certificate ID must be provided for non-root certificates");
            }
            issuerCertEntity = certificateRepository.findById(UUID.fromString(dto.getIssuerCertificateId()))
                    .orElseThrow(() -> new IllegalArgumentException("Issuer certificate not found"));

            if (!isCertificateValid(issuerCertEntity)) {
                throw new IllegalArgumentException("Issuer certificate is not valid");
            }

            X509Certificate issuerCert = issuerCertEntity.getX509Certificate();
            PrivateKey decriptedIssuerPrivateKey = loadAndDecryptPrivateKeyForIssuer(issuerCertEntity);
            issuer = new Issuer(decriptedIssuerPrivateKey, CertificateUtils.getSubjectX500Name(issuerCert));
        }

        if (dto.getType() == CertificateType.END_ENTITY && dto.getIssuerCertificateId() == null) {
            throw new IllegalArgumentException("End-entity certificate must have an issuer");
        }

        if (issuerCertEntity != null && issuerCertEntity.getType() == CertificateType.END_ENTITY) {
            throw new IllegalArgumentException("End-entity certificates cannot act as issuers");
        }

        // --- 5. Generate X509 certificate ---
        X509Certificate x509Certificate = CertificateUtils.generateCertificate(
                subject,
                issuer,
                dto.getStartDate(),
                dto.getEndDate(),
                new BigInteger(64, new SecureRandom()).toString(), // Serial number
                dto.getType(),
                dto.getExtensions()
        );

        // --- 6. Encrypt private key with DEK-om ---
        SecretKey organizationDEK = getOrganizationDEK(organization);
        AESUtils.AESGcmEncrypted encryptedSubjectPrivateKey = aesUtils.encrypt(organizationDEK,
                Base64.getEncoder().encodeToString(subjectPrivateKey.getEncoded()));

        // --- 7. Create Certificate ---
        Certificate certificateEntity = new Certificate();
        certificateEntity.setOrganization(organization);
        certificateEntity.setType(dto.getType());
        certificateEntity.setSerialNumber(x509Certificate.getSerialNumber().toString());
        certificateEntity.setStartDate(dto.getStartDate());
        certificateEntity.setEndDate(dto.getEndDate());
        certificateEntity.setCertificateEncoded(x509Certificate.getEncoded());
        certificateEntity.setPrivateKeyEncrypted(Base64.getDecoder().decode(encryptedSubjectPrivateKey.getCiphertext()));
        certificateEntity.setIv(encryptedSubjectPrivateKey.getIv());
        certificateEntity.setIssuer(issuerCertEntity);

        if (!isCertificateValid(certificateEntity)) {
            throw new IllegalArgumentException("Generated certificate is not valid");
        }

        return certificateRepository.save(certificateEntity);
    }


    public Collection<SimpleCertificateDTO> findAllCAForMyOrganization() {
        User currentUser = userService.getLoggedUser();
        List<CertificateType> caTypes = List.of(CertificateType.ROOT, CertificateType.INTERMEDIATE);
        List<Certificate> certs = certificateRepository.findAllByOrganizationAndTypeIn(currentUser.getOrganization(), caTypes);
        ArrayList<SimpleCertificateDTO> dtos = new ArrayList<>();
        for (Certificate cert : certs) {
            try {
                if (isCertificateValid(cert)) {
                    dtos.add(new SimpleCertificateDTO(
                            cert.getId(),
                            cert.getSerialNumber(),
                            CertificateUtils.getSubjectX500Name(cert.getX509Certificate())
                                    .getRDNs(BCStyle.CN)[0].getFirst().getValue().toString(),
                            cert.getEndDate()
                    ));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating certificate: " + e.getMessage());
            }
        }
        return dtos;
    }

    public boolean isCertificateValid(Certificate certificate) throws Exception {
        X509Certificate x509Certificate = certificate.getX509Certificate();
        boolean isValidByDate = CertificateUtils.isValidByDate(x509Certificate);
        boolean isSignatureValid = CertificateUtils.isValidSignature(x509Certificate,
                certificate.getIssuer() != null ? certificate.getIssuer().getX509Certificate() : x509Certificate);
        boolean isRevoked = certificate.isRevoked();

        if (certificate.getType() == CertificateType.ROOT) {
            return isValidByDate && isSignatureValid && !isRevoked;
        }

        boolean isIssuerValid = isCertificateValid(certificate.getIssuer());
        return isValidByDate && isSignatureValid && !isRevoked && isIssuerValid;
    }

    private SecretKey getOrganizationDEK(Organization organization) throws Exception {
        String encryptedDEKBase64 = organization.getEncryptedOrgKey();
        AESUtils.AESGcmEncrypted encrypted = new AESUtils.AESGcmEncrypted(encryptedDEKBase64, organization.getOrgKeyIv());
        String dekBase64 = aesUtils.decrypt(masterKey, encrypted);
        return AESUtils.secretKeyFromBase64(dekBase64);
    }

    private PrivateKey loadAndDecryptPrivateKeyForIssuer(Certificate issuerCertEntity) throws Exception {
        byte[] encryptedPrivateKeyBytes = issuerCertEntity.getPrivateKeyEncrypted();
        String iv = issuerCertEntity.getIv();

        SecretKey organizationDEK = getOrganizationDEK(issuerCertEntity.getOrganization());
        AESUtils.AESGcmEncrypted encryptedPrivateKey = new AESUtils.AESGcmEncrypted(
                Base64.getEncoder().encodeToString(encryptedPrivateKeyBytes), iv);

        String decryptedPrivateKeyBase64 = aesUtils.decrypt(organizationDEK, encryptedPrivateKey);
        return RSAUtils.base64ToPrivateKey(decryptedPrivateKeyBase64);

    }
}

