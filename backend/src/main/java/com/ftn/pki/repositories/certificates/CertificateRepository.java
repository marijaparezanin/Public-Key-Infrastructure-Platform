package com.ftn.pki.repositories.certificates;

import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.models.organizations.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    List<Certificate> findAllByOrganizationAndTypeIn(Organization organization, List<CertificateType> types);

    List<Certificate> findAllByOrganizationId(UUID id);

    List<Certificate> findAllByUserId(UUID id);

    List<Certificate> findAllByIssuerId(UUID id);

    List<Certificate> findAllByTypeIn(List<CertificateType> caTypes);

    Optional<Certificate> findByType(CertificateType type);
}
