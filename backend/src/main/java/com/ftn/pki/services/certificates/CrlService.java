package com.ftn.pki.services.certificates;

import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CrlEntry;
import com.ftn.pki.repositories.certificates.CrlEntryRepository;
import com.ftn.pki.utils.cryptography.AESUtils;
import com.ftn.pki.utils.cryptography.RSAUtils;
import org.bouncycastle.asn1.x500.X500Name;
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

@Service
public class CrlService {

    private final CrlEntryRepository crlEntryRepository;

    public CrlService(CrlEntryRepository crlEntryRepository) {
        this.crlEntryRepository = crlEntryRepository;
    }


    public X509CRLHolder generateCRL(X509Certificate issuerCert, PrivateKey issuerKey) throws Exception {
        X500Name issuerName = new X500Name(issuerCert.getSubjectX500Principal().getName());

        // CRL valid from now to +7 days (možeš menjati)
        Date now = new Date();
        Date nextUpdate = new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000);

        X509v2CRLBuilder crlBuilder = new JcaX509v2CRLBuilder(issuerCert, now);
        crlBuilder.setNextUpdate(nextUpdate);

        for (CrlEntry entry : crlEntryRepository.findAll()) {
            crlBuilder.addCRLEntry(
                    new BigInteger(entry.getCertificateSerialNumber()),
                    entry.getRevocationDate(),
                    entry.getReason().ordinal() // ili mapiraj na ASN.1 reason code
            );
        }

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey);
        return crlBuilder.build(contentSigner);
    }
}
