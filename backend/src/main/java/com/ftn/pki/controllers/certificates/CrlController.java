package com.ftn.pki.controllers.certificates;

import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.repositories.certificates.CertificateRepository;
import com.ftn.pki.services.certificates.CrlService;
import org.bouncycastle.cert.X509CRLHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/api/crl")
public class CrlController {

    private final CrlService crlService;
    private final CertificateRepository certificateRepository;

    public CrlController(CrlService crlService, CertificateRepository certificateRepository) {
        this.crlService = crlService;
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/latest")
    public ResponseEntity<byte[]> getCrl() throws Exception {
        // Uzmi root CA cert kao issuer
        Certificate issuer = certificateRepository.findByType(CertificateType.ROOT)
                .orElseThrow(() -> new IllegalStateException("Root CA not found"));
        X509Certificate issuerCert = issuer.getX509Certificate();
        PrivateKey issuerKey = crlService.loadAndDecryptPrivateKey(issuer);

        X509CRLHolder crlHolder = crlService.generateCRL(issuerCert, issuerKey);

        byte[] crlBytes = crlHolder.getEncoded();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=crl.pem")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(crlBytes);
    }
}
