package com.ftn.pki.repositories.certificates;


import com.ftn.pki.models.certificates.CrlEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CrlEntryRepository extends JpaRepository<CrlEntry, java.util.UUID> {
    List<CrlEntry> findAll();

    List<CrlEntry> findByIssuerId(UUID issuerId);

}
