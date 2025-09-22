package com.ftn.pki.services.organizations;

import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.repositories.organizations.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {


    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization findOrganizationByName(String name) {
        return organizationRepository.findByName(name);
    }

    public Organization save(Organization data) {
        return this.organizationRepository.save(data);
    }

    public Organization registerOrganization(String organizationName) {
        Organization organization = new Organization();
        organization.setName(organizationName);
        return this.save(organization);
    }
}
