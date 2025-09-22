package com.ftn.pki.repositories.certificates;

import com.ftn.pki.models.certificates.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
}
