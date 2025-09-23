package com.ftn.pki.repositories.organizations;

import com.ftn.pki.models.organizations.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Organization findByName(String name);
}
