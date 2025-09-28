package com.ftn.pki.controllers.certificates;

import com.ftn.pki.dtos.certificates.CreateCertificateTemplateDTO;
import com.ftn.pki.dtos.certificates.SimpleCertificateTemplateDTO;
import com.ftn.pki.services.certificates.CertificateTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/certificates/templates")
public class CertificateTemplateController {


    private final CertificateTemplateService certificateTemplateService;

    public CertificateTemplateController(CertificateTemplateService certificateTemplateService) {
        this.certificateTemplateService = certificateTemplateService;
    }

    @PostMapping()
    public ResponseEntity<Void> createCertificateTemplate(@RequestBody CreateCertificateTemplateDTO dto) {
        try {
            certificateTemplateService.createTemplate(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/ca/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Collection<SimpleCertificateTemplateDTO>> getAllTemplatesForCA(@PathVariable String id) {
        try {
            Collection<SimpleCertificateTemplateDTO> caCertificates = certificateTemplateService.findAllByIssuerId(id);
            return ResponseEntity.ok(caCertificates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<Boolean> getTemplateNameExists(@PathVariable String name) {
        try {
            boolean exists = certificateTemplateService.findByName(name);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

}
