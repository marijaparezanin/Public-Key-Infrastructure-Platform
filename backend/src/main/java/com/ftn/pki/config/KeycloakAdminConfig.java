package com.ftn.pki.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${KEYCLOAK_URL}")
    private String keycloakUrl;

    @Value("${KEYCLOAK_REALM}")
    private String keycloakRealm;

    @Value("${KEYCLOAK_CLIENT_ID}")
    private String keycloakClientId;

    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String keycloakClientSecret;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(keycloakRealm)
                .clientId(keycloakClientId)
                .clientSecret(keycloakClientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
