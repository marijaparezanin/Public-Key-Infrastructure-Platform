package com.ftn.pki.services.organizations;

import com.ftn.pki.dtos.ogranizations.SimpleOrganizationDTO;
import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.repositories.organizations.OrganizationRepository;
import com.ftn.pki.utils.cryptography.AESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final AESUtils aesUtils;
    private final SecretKey masterKey;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, AESUtils aesUtils,
                               @Value("${MASTER_KEY}") String base64MasterKey) {
        this.aesUtils = aesUtils;
        this.organizationRepository = organizationRepository;
        this.masterKey = AESUtils.secretKeyFromBase64(base64MasterKey);
    }

    public Organization findOrganizationByName(String name) {
        return organizationRepository.findByName(name);
    }

    public List<SimpleOrganizationDTO> findAllSimpleDTO() {
        List<Organization> organizations = organizationRepository.findAll();
        return organizations.stream()
                .map(org -> new SimpleOrganizationDTO(org.getId(), org.getName()))
                .toList();
    }

    public Organization save(Organization data) {
        return this.organizationRepository.save(data);
    }

    public Organization registerOrganization(String organizationName) throws Exception {
        Organization organization = new Organization();
        organization.setName(organizationName);

        SecretKey organizationKey = aesUtils.generateRandomKey();

        AESUtils.AESGcmEncrypted encryptedDek = aesUtils.encrypt(masterKey, Base64.getEncoder().encodeToString(organizationKey.getEncoded()));
        organization.setEncryptedOrgKey(encryptedDek.getCiphertext());
        organization.setOrgKeyIv(encryptedDek.getIv());
        return this.save(organization);
    }
}
