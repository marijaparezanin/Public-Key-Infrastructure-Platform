package com.ftn.pki.services.users;

import com.ftn.pki.dtos.users.CreateCAUserDTO;
import com.ftn.pki.models.users.User;
import com.ftn.pki.repositories.users.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;
import org.springframework.stereotype.Service;
import org.keycloak.admin.client.Keycloak;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;
    private final String realm = "pki";


    public UserService(UserRepository userRepository, Keycloak keycloak) {
        this.userRepository = userRepository;
        this.keycloak = keycloak;
    }

    public User getLoggedUser() {
        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return this.findUserByEmail(token.getTokenAttributes().get("email").toString());
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    public User save(User data) {
        return this.userRepository.save(data);
    }





    public void createCAUser(CreateCAUserDTO dto) {
        RealmResource realmResource = keycloak.realm(realm);

        // 1. Create user
        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getEmail());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getName());
        user.setLastName(dto.getSurname());
        user.setEnabled(true);

        var response = realmResource.users().create(user);
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create CA user. Status: " + response.getStatus());
        }

        // Get created user ID
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // 2. Assign CA role
        var realmRole = realmResource.roles().get("ca").toRepresentation();
        realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(realmRole));

        // 3. Trigger UPDATE_PASSWORD email
        realmResource.users().get(userId)
                .executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));
    }
}
