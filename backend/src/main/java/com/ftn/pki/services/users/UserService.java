package com.ftn.pki.services.users;

import com.ftn.pki.dtos.users.CreateCAUserDTO;
import com.ftn.pki.models.users.User;
import com.ftn.pki.repositories.users.UserRepository;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.*;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;
    private final String realm = "pki";
    private final String clientId = "pki-frontend";


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

        List<UserRepresentation> existingUsers = realmResource.users().search(dto.getEmail());
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("User with email " + dto.getEmail() + " already exists");
        }

        // 2. Create user
        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getEmail());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getName());
        user.setLastName(dto.getSurname());
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setAttributes(Collections.singletonMap("organization", Collections.singletonList(dto.getOrganization())));

        Response response = realmResource.users().create(user);
        if (response.getStatus() != 201) {
            String errorMessage = response.readEntity(String.class);
            throw new RuntimeException("Failed to create CA user. Status: " + response.getStatus() + ", Response: " + errorMessage);
        }

        // 3. Get user ID
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        System.out.println("Created user ID: " + userId);

        // 4. Assign client role "ca"
        try {
            // Find the client by clientId
            List<ClientRepresentation> clients = realmResource.clients().findByClientId(clientId);
            if (clients.isEmpty()) {
                throw new RuntimeException("Client '" + clientId + "' not found in realm 'pki'");
            }
            String clientUuid = clients.get(0).getId();

            // Fetch the "ca" role from the client
            RoleRepresentation clientRole = realmResource.clients().get(clientUuid).roles().get("ca_user").toRepresentation();

            // Assign the client role to the user
            realmResource.users().get(userId).roles().clientLevel(clientUuid).add(Collections.singletonList(clientRole));
            System.out.println("Assigned client role 'ca' to user: " + userId);
        } catch (NotFoundException e) {
            // Log available client roles for debugging
            List<ClientRepresentation> clients = realmResource.clients().findByClientId(clientId);
            if (!clients.isEmpty()) {
                List<RoleRepresentation> roles = realmResource.clients().get(clients.get(0).getId()).roles().list();
                System.out.println("Available client roles for '" + clientId + "': " + roles.stream().map(RoleRepresentation::getName).toList());
            }
            throw new RuntimeException("Client role 'ca' not found for client '" + clientId + "' in realm 'pki'", e);
        }

        // 5. Set UPDATE_PASSWORD
        UserResource userResource = realmResource.users().get(userId);
        UserRepresentation userRep = userResource.toRepresentation();
        userRep.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));
        userResource.update(userRep);

        // 6. Send email
        try {
            userResource.executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));
            System.out.println("Email sent successfully for user: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

}
