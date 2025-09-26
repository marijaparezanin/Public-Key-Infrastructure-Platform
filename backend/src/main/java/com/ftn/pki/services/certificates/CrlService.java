package com.ftn.pki.services.certificates;

import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CrlEntry;
import com.ftn.pki.models.certificates.RevocationReason;
import com.ftn.pki.repositories.certificates.CrlEntryRepository;
import com.ftn.pki.utils.cryptography.AESUtils;
import com.ftn.pki.utils.cryptography.RSAUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v2CRLBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class CrlService {

    private final CrlEntryRepository crlEntryRepository;

    public CrlService(CrlEntryRepository crlEntryRepository) {
        this.crlEntryRepository = crlEntryRepository;
    }


    public X509CRLHolder generateCRL(Certificate issuerCert, PrivateKey issuerKey) throws Exception {
        // 2. Define CRL validity period (now -> +7 days)
        Date now = new Date();
        Date nextUpdate = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000); // 7 days

        // 3. Initialize CRL builder
        X509v2CRLBuilder crlBuilder = new JcaX509v2CRLBuilder(issuerCert.getX509Certificate(), now);
        crlBuilder.setNextUpdate(nextUpdate);

        // 4. Fetch only entries revoked by this issuer
        UUID issuerId = issuerCert.getId();
        for (CrlEntry entry : crlEntryRepository.findByIssuerId(issuerId)) {
            crlBuilder.addCRLEntry(
                    new BigInteger(entry.getCertificateSerialNumber()),
                    entry.getRevocationDate(),
                    mapReason(entry.getReason()) // Map to standard ASN.1 reason code
            );
        }

        // 5. Add CRL extensions (optional, e.g., Authority Key Identifier)
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        crlBuilder.addExtension(
                Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(issuerCert.getX509Certificate())
        );

        // 6. Sign the CRL
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey);
        return crlBuilder.build(contentSigner);
    }

    // Helper to map your internal reason enum to RFC 5280 standard codes
    private int mapReason(RevocationReason reason) {
        return switch (reason) {
            case KEY_COMPROMISE -> CRLReason.keyCompromise;
            case CA_COMPROMISE -> CRLReason.cACompromise;
            case AFFILIATION_CHANGED -> CRLReason.affiliationChanged;
            case SUPERSEDED -> CRLReason.superseded;
            case CESSATION_OF_OPERATION -> CRLReason.cessationOfOperation;
            case CERTIFICATE_HOLD -> CRLReason.certificateHold;
            default -> CRLReason.unspecified;
        };
    }

}
