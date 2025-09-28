package com.ftn.pki.controllers.certificates;

import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.repositories.certificates.CertificateRepository;
import com.ftn.pki.services.certificates.CertificateService;
import com.ftn.pki.services.certificates.CrlService;
import org.bouncycastle.cert.X509CRLHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

@RestController
@RequestMapping("/api/crl")
public class CrlController {

    private final CrlService crlService;
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;

    public CrlController(CrlService crlService, CertificateRepository certificateRepository, CertificateService certificateService) {
        this.crlService = crlService;
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
    }

    @GetMapping("/{issuerId}/latest")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getCrl(@PathVariable UUID issuerId) {
        try {
            Certificate issuer = certificateRepository.findById(issuerId)
                    .orElseThrow(() -> new IllegalStateException("Issuer not found"));

            PrivateKey issuerKey = certificateService.loadAndDecryptPrivateKey(issuer);

            X509CRLHolder crlHolder = crlService.generateCRL(issuer, issuerKey);
            byte[] crlBytes = crlHolder.getEncoded();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + issuerId + "-crl.der")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(crlBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage().getBytes());
        }
    }


}
