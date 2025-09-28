package com.ftn.pki.repositories.certificates;

import com.ftn.pki.models.certificates.CertificateTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, UUID> {
    List<CertificateTemplate> findAll();

    List<CertificateTemplate> findByIssuerId(UUID issuerId);

    List<CertificateTemplate> findByOrOrganizationName(String organizationName);

    Optional<CertificateTemplate> findByName(String name);
}
