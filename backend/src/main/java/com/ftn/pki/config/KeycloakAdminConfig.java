package com.ftn.pki.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(System.getenv("KEYCLOAK_URL"))
                .realm(System.getenv("KEYCLOAK_REALM"))
                .clientId(System.getenv("KEYCLOAK_CLIENT_ID"))      // service account client
                .clientSecret(System.getenv("KEYCLOAK_CLIENT_SECRET"))
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
