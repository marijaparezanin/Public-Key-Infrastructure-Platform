package com.ftn.pki.repositories.certificates;

import com.ftn.pki.models.certificates.Certificate;
import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.models.organizations.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    List<Certificate> findAllByOrganizationAndTypeIn(Organization organization, List<CertificateType> types);
}
