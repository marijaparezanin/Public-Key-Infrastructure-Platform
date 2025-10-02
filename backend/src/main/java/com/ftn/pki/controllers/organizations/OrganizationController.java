package com.ftn.pki.controllers.organizations;

import com.ftn.pki.dtos.ogranizations.SimpleOrganizationDTO;
import com.ftn.pki.services.organizations.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Collection<SimpleOrganizationDTO>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.findAllSimpleDTO());
    }
}
