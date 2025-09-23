package com.ftn.pki.controllers.certificates;

import com.ftn.pki.dtos.certificates.CreateCertificateDTO;
import com.ftn.pki.dtos.certificates.CreatedCertificateDTO;
import com.ftn.pki.dtos.certificates.SimpleCertificateDTO;
import com.ftn.pki.services.certificates.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    @Autowired
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping()
    public ResponseEntity<CreatedCertificateDTO> createCertificate(@RequestBody CreateCertificateDTO dto) {
        try {
            CreatedCertificateDTO certificate = certificateService.createCertificate(dto);
            return ResponseEntity.ok(certificate);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/applicable-ca")
    public ResponseEntity<Collection<SimpleCertificateDTO>> getAllCAForOrganization() {
        try {
            Collection<SimpleCertificateDTO> caCertificates = certificateService.findAllCAForMyOrganization();
            return ResponseEntity.ok(caCertificates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Collection<SimpleCertificateDTO>> getAll(){
        return ResponseEntity.ok(certificateService.findAllSimple());
    }

    @GetMapping("/revoke/{id}")
    public ResponseEntity<Void> revokeCertificate(@PathVariable String id){
        try {
            certificateService.revokeCertificate(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

}
