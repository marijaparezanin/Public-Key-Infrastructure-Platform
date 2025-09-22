package com.ftn.pki.services.certificates;

import com.ftn.pki.dtos.certificates.CreateCertificateDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateUtils certificateUtils;
    private final AESUtils aesUtils;
    private final RSAUtils rsaUtils;
    private final UserService userService;
    private final SecretKey masterKey = AESUtils.secretKeyFromBase64(System.getenv("MASTER_KEY"));

    @Autowired
    public CertificateService(CertificateRepository certificateRepository,
                              CertificateUtils certificateUtils,
                              AESUtils aesUtils, UserService userService,
                              RSAUtils rsaUtils) {
        this.certificateRepository = certificateRepository;
        this.certificateUtils = certificateUtils;
        this.aesUtils = aesUtils;
        this.userService = userService;
        this.rsaUtils = rsaUtils;
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
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.CN, dto.getCommonName())
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.SURNAME, dto.getSurname())
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.GIVENNAME, dto.getGivenName())
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.O, dto.getOrganization())
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.OU, dto.getOrganizationalUnit())
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.C, dto.getCountry())
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.E, dto.getEmail())
                .build();

        Subject subject = new Subject(subjectPublicKey, subjectX500);

        // --- 4. Fetch Issuer ---
        Issuer issuer;
        if (dto.getIssuerCertificateId() == null && dto.getType() == CertificateType.ROOT) {
            // Self-signed Root
            issuer = new Issuer(subjectPrivateKey, subjectX500);
        } else {
            if (dto.getIssuerCertificateId() == null) {
                throw new IllegalArgumentException("Issuer certificate ID must be provided for non-root certificates");
            }
            Certificate issuerCertEntity = certificateRepository.findById(UUID.fromString(dto.getIssuerCertificateId()))
                    .orElseThrow(() -> new IllegalArgumentException("Issuer certificate not found"));

            X509Certificate issuerCert = issuerCertEntity.getX509Certificate();
            PrivateKey decriptedIssuerPrivateKey = loadAndDecryptPrivateKeyForIssuer(issuerCertEntity);
            issuer = new Issuer(decriptedIssuerPrivateKey, CertificateUtils.getSubjectX500Name(issuerCert));
        }

        // --- 5. Generate X509 certificate ---
        X509Certificate x509Certificate = CertificateUtils.generateCertificate(
                subject,
                issuer,
                dto.getStartDate(),
                dto.getEndDate(),
                new java.math.BigInteger(64, new SecureRandom()).toString() // Serial number
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
        certificateEntity.setExtensionsJson(dto.getExtensions() != null ? String.join(",", dto.getExtensions()) : "");

        return certificateRepository.save(certificateEntity);
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

