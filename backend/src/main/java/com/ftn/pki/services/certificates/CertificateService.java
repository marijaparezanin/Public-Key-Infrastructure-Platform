package com.ftn.pki.services.certificates;

import com.ftn.pki.dtos.certificates.*;
import com.ftn.pki.models.certificates.*;
import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.models.users.User;
import com.ftn.pki.models.users.UserRole;
import com.ftn.pki.repositories.certificates.CertificateRepository;
import com.ftn.pki.repositories.certificates.CertificateTemplateRepository;
import com.ftn.pki.repositories.certificates.CrlEntryRepository;
import com.ftn.pki.services.organizations.OrganizationService;
import com.ftn.pki.services.users.UserService;
import com.ftn.pki.utils.certificates.CertificateUtils;
import com.ftn.pki.utils.cryptography.AESUtils;
import com.ftn.pki.utils.cryptography.RSAUtils;
import jakarta.transaction.Transactional;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.ftn.pki.utils.certificates.CertificateUtils.getRDNValue;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository certificateTemplateRepository;
    private final CrlEntryRepository crlEntryRepository;
    private final AESUtils aesUtils;
    private final UserService userService;
    private final SecretKey masterKey;
    private final OrganizationService organizationService;

    @Autowired
    public CertificateService(CertificateRepository certificateRepository, CertificateTemplateRepository certificateTemplateRepository, CrlEntryRepository crlEntryRepository,
                              AESUtils aesUtils, UserService userService,
                              @Value("${MASTER_KEY}") String base64MasterKey, OrganizationService organizationService) {
        this.certificateRepository = certificateRepository;
        this.certificateTemplateRepository = certificateTemplateRepository;
        this.crlEntryRepository = crlEntryRepository;
        this.aesUtils = aesUtils;
        this.userService = userService;
        this.masterKey = AESUtils.secretKeyFromBase64(base64MasterKey);
        this.organizationService = organizationService;
    }

    @Transactional
    public CreatedCertificateDTO createCertificate(CreateCertificateDTO dto) throws Exception {
        User currentUser = userService.getLoggedUser();
        Organization organization = null;
        if (currentUser.getRole() == UserRole.ROLE_admin && dto.getType() == CertificateType.INTERMEDIATE) {
            organization = organizationService.findOrganizationByName(dto.getAssignToOrganizationName());
        } else {
            organization = currentUser.getOrganization();
        }
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
        if (dto.getIssuerCertificateId().isEmpty() && dto.getType() == CertificateType.ROOT) {
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
            PrivateKey decriptedIssuerPrivateKey = loadAndDecryptPrivateKey(issuerCertEntity);
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
                dto.getIssuerCertificateId(),
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
        certificateEntity.setRevoked(false);
        certificateEntity.setUser(currentUser);

        if (!isCertificateValid(certificateEntity)) {
            throw new IllegalArgumentException("Generated certificate is not valid");
        }

        certificateRepository.save(certificateEntity);

        return new CreatedCertificateDTO(
                certificateEntity.getId(),
                certificateEntity.getType(),
                dto.getCommonName(),
                dto.getSurname(),
                dto.getGivenName(),
                dto.getOrganization(),
                dto.getOrganizationalUnit(),
                dto.getCountry(),
                dto.getEmail(),
                certificateEntity.getStartDate(),
                certificateEntity.getEndDate(),
                dto.getExtensions()
        );
    }

    public Collection<SimpleCertificateDTO> findAllCAForMyOrganization() {
        User currentUser = userService.getLoggedUser();
        List<CertificateType> caTypes = List.of(CertificateType.ROOT, CertificateType.INTERMEDIATE);
        List<Certificate> certs = null;
        if (currentUser.getRole() == UserRole.ROLE_admin) {
            certs = certificateRepository.findAllByTypeIn(caTypes);
        }else {
            certs = certificateRepository.findAllByOrganizationAndTypeIn(currentUser.getOrganization(), caTypes);
        }
        ArrayList<SimpleCertificateDTO> dtos = new ArrayList<>();
        for (Certificate cert : certs) {
            try {
                if (isCertificateValid(cert)) {
                    X500Name subjectX500Name = CertificateUtils.getSubjectX500Name(cert.getX509Certificate());
                    dtos.add(new SimpleCertificateDTO(
                            cert.getId(),
                            cert.getType(), // CertificateType
                            getRDNValue(subjectX500Name, BCStyle.CN),
                            getRDNValue(subjectX500Name, BCStyle.SURNAME),
                            getRDNValue(subjectX500Name, BCStyle.GIVENNAME),
                            getRDNValue(subjectX500Name, BCStyle.O),
                            getRDNValue(subjectX500Name, BCStyle.OU),
                            getRDNValue(subjectX500Name, BCStyle.C),
                            getRDNValue(subjectX500Name, BCStyle.E),
                            cert.getStartDate(),
                            cert.getEndDate(),
                            cert.isRevoked(),
                            isCertificateValid(cert),
                            cert.getSerialNumber()
                            )
                    );
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

    public PrivateKey loadAndDecryptPrivateKey(Certificate certEntity) throws Exception {
        byte[] encryptedPrivateKeyBytes = certEntity.getPrivateKeyEncrypted();
        String iv = certEntity.getIv();

        SecretKey organizationDEK = getOrganizationDEK(certEntity.getOrganization());
        AESUtils.AESGcmEncrypted encryptedPrivateKey = new AESUtils.AESGcmEncrypted(
                Base64.getEncoder().encodeToString(encryptedPrivateKeyBytes), iv);

        String decryptedPrivateKeyBase64 = aesUtils.decrypt(organizationDEK, encryptedPrivateKey);
        return RSAUtils.base64ToPrivateKey(decryptedPrivateKeyBase64);

    }

    @Transactional
    public Collection<SimpleCertificateDTO> findAllSimple(){
        List<Certificate> certs = null;
        ArrayList<SimpleCertificateDTO> dtos = new ArrayList<>();
        switch (userService.getLoggedUser().getRole()){
            case ROLE_ee_user -> {
                certs = certificateRepository.findAllByUserId(userService.getLoggedUser().getId());
            }
            case ROLE_ca_user -> {
                certs = certificateRepository.findAllByOrganizationId(userService.getLoggedUser().getOrganization().getId());
            }
            case ROLE_admin -> {
                certs = certificateRepository.findAll();
            }
            default -> throw new IllegalArgumentException("Unknown role");
        }

        for (Certificate cert : certs) {
            try {
                X500Name subjectX500Name = CertificateUtils.getSubjectX500Name(cert.getX509Certificate());
                SimpleCertificateDTO dto = new SimpleCertificateDTO(
                        cert.getId(),
                        cert.getType(), // CertificateType
                        getRDNValue(subjectX500Name, BCStyle.CN),
                        getRDNValue(subjectX500Name, BCStyle.SURNAME),
                        getRDNValue(subjectX500Name, BCStyle.GIVENNAME),
                        getRDNValue(subjectX500Name, BCStyle.O),
                        getRDNValue(subjectX500Name, BCStyle.OU),
                        getRDNValue(subjectX500Name, BCStyle.C),
                        getRDNValue(subjectX500Name, BCStyle.E),
                        cert.getStartDate(),
                        cert.getEndDate(),
                        cert.isRevoked(),
                        isCertificateValid(cert),
                        cert.getSerialNumber()
                );
                dtos.add(dto);
            } catch (Exception e) {
                throw new RuntimeException("Error validating certificate: " + e.getMessage());
            }
        }
        return dtos;
    }

    @Transactional
    public void revokeCertificate(RequestRevokeDTO dto) {
        Certificate cert = certificateRepository.findById(dto.getCertificateId())
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));

        User currentUser = userService.getLoggedUser();
        if (currentUser.getRole() == UserRole.ROLE_ee_user) {
            if (!cert.getUser().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("EE users can only revoke their own certificates");
            }
        } else if (currentUser.getRole() == UserRole.ROLE_ca_user) {
            if (!cert.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new IllegalArgumentException("CA users can only revoke certificates within their organization");
            }
        } else if (currentUser.getRole() != UserRole.ROLE_admin) {
            throw new IllegalArgumentException("Unknown user role");
        }

        if (cert.isRevoked()) {
            throw new IllegalArgumentException("Certificate is already revoked");
        }

        cert.setRevoked(true);
        cert.setRevocationReason(dto.getReason());
        addToCrl(cert);
        certificateRepository.save(cert);


        for (Certificate childCert : certificateRepository.findAllByIssuerId(cert.getId())) {
            if (!childCert.isRevoked()) {
                revokeCertificate(new RequestRevokeDTO(childCert.getId(), dto.getReason()));
            }
        }
    }

    private void addToCrl(Certificate cert) {
        CrlEntry crlEntry = new CrlEntry();
        crlEntry.setCertificateSerialNumber(cert.getSerialNumber());
        crlEntry.setRevocationDate(new Date());
        crlEntry.setReason(cert.getRevocationReason());
        if (cert.getType() == CertificateType.ROOT){
            crlEntry.setIssuerId(cert.getId());
        } else {
            crlEntry.setIssuerId(cert.getIssuer().getId());
        }
        crlEntryRepository.save(crlEntry);
    }


    public byte[] getKeyStoreForDownload(DownloadRequestDTO dto) throws Exception {
        Certificate cert = certificateRepository.findById(dto.getCertificateId())
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));

        User currentUser = userService.getLoggedUser();
        if (currentUser.getRole() == UserRole.ROLE_ee_user) {
            if (!cert.getUser().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("EE users can only download their own certificates");
            }
        } else if (currentUser.getRole() == UserRole.ROLE_ca_user) {
            if (!cert.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new IllegalArgumentException("CA users can only download certificates within their organization");
            }
        } else if (currentUser.getRole() != UserRole.ROLE_admin) {
            throw new IllegalArgumentException("Unknown user role");
        }

        X509Certificate x509Certificate = cert.getX509Certificate();
        PrivateKey privateKey = loadAndDecryptPrivateKey(cert);

        KeyStore ks = switch (dto.getFormat()) {
            case PKCS12 -> KeyStore.getInstance("PKCS12");
            case JKS -> KeyStore.getInstance("JKS");
            default -> throw new IllegalArgumentException("Unknown format: " + dto.getFormat());
        };
        ks.load(null, null);
        ks.setKeyEntry(
                dto.getAlias(),
                privateKey,
                dto.getPassword().toCharArray(),
                new java.security.cert.Certificate[]{x509Certificate}
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ks.store(baos, dto.getPassword().toCharArray());


        return baos.toByteArray();
    }




    @Transactional
    public void createTemplate(CreateCertificateTemplateDTO dto) throws Exception {
        User currentUser = userService.getLoggedUser();
        Organization organization = currentUser.getOrganization();
        System.out.println("Organization: " + organization.getName());
        System.out.println("Organization2: " + organization);
        
        Certificate issuerCertEntity = null;
        if (dto.getIssuerCertificateId() == null) {
            throw new IllegalArgumentException("Issuer certificate ID must be provided for non-root certificates");
        }
        issuerCertEntity = certificateRepository.findById(dto.getIssuerCertificateId())
                .orElseThrow(() -> new IllegalArgumentException("Issuer certificate not found"));

        if (!isCertificateValid(issuerCertEntity)) {
            throw new IllegalArgumentException("Issuer certificate is not valid");
        }


        // --- 7. Create Certificate ---
        CertificateTemplate certificateTemplate = new CertificateTemplate();
        certificateTemplate.setName(dto.getName());
        certificateTemplate.setIssuer(issuerCertEntity);
        certificateTemplate.setOrganization(organization);
        certificateTemplate.setCommonNameRegex(dto.getCommonNameRegex());
        certificateTemplate.setSubjectAlternativeNameRegex(dto.getSubjectAlternativeNameRegex());
        certificateTemplate.setTtlDays(dto.getTtlDays());
        certificateTemplate.setKeyUsage(dto.getKeyUsage());
        certificateTemplate.setExtendedKeyUsage(dto.getExtendedKeyUsage());


        certificateTemplateRepository.save(certificateTemplate);
    }

}

