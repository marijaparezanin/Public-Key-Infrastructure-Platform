package com.ftn.pki.repositories.certificates;


import com.ftn.pki.models.certificates.CrlEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrlEntryRepository extends JpaRepository<CrlEntry, java.util.UUID> {
    List<CrlEntry> findAll();
}
