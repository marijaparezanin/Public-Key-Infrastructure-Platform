package com.ftn.pki.services.certificates;

import com.ftn.pki.dtos.certificates.CreateCertificateTemplateDTO;
import com.ftn.pki.dtos.certificates.SimpleCertificateDTO;
import com.ftn.pki.dtos.certificates.SimpleCertificateTemplateDTO;
import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CertificateTemplate;
import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.models.users.User;
import com.ftn.pki.repositories.certificates.CertificateRepository;
import com.ftn.pki.repositories.certificates.CertificateTemplateRepository;
import com.ftn.pki.services.users.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class CertificateTemplateService {
    private final CertificateTemplateRepository certificateTemplateRepository;
    private final UserService userService;
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;


    public CertificateTemplateService(CertificateTemplateRepository certificateTemplateRepository, UserService userService, CertificateRepository certificateRepository, CertificateService certificateService) {
        this.certificateTemplateRepository = certificateTemplateRepository;
        this.userService = userService;
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
    }


    public boolean findByName(String name){
        Optional<CertificateTemplate>  certificateTemplate = certificateTemplateRepository.findByName(name);
        return certificateTemplate.isPresent();
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

        if (!certificateService.isCertificateValid(issuerCertEntity)) {
            throw new IllegalArgumentException("Issuer certificate is not valid");
        }
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

    public Collection<SimpleCertificateTemplateDTO> findAllByIssuerId(String issuerId) {
        ArrayList<SimpleCertificateTemplateDTO> dtos = new ArrayList<>();
        Collection<CertificateTemplate> temps = certificateTemplateRepository.findByIssuerId(UUID.fromString(issuerId));
        for (CertificateTemplate cert : temps) {
            SimpleCertificateTemplateDTO simpleCertificateTemplateDTO = new SimpleCertificateTemplateDTO();
            simpleCertificateTemplateDTO.setName(cert.getName());
            simpleCertificateTemplateDTO.setCommonNameRegex(cert.getCommonNameRegex());
            simpleCertificateTemplateDTO.setKeyUsage(cert.getKeyUsage());
            simpleCertificateTemplateDTO.setTtlDays(cert.getTtlDays());
            simpleCertificateTemplateDTO.setExtendedKeyUsage(cert.getExtendedKeyUsage());
            simpleCertificateTemplateDTO.setSubjectAlternativeNameRegex(cert.getSubjectAlternativeNameRegex());
            dtos.add(simpleCertificateTemplateDTO);
        }
        return dtos;
    }
}
