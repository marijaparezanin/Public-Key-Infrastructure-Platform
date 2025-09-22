package org.pwned;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

public class PwnedPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {

    public static final String PROVIDER_ID = "pwned-password-policy";
    private static final int DEFAULT_MAX_OCCURRENCES = 0;

    @Override
    public PasswordPolicyProvider create(KeycloakSession keycloakSession) {
        // Use default config or implement logic to fetch config if needed
        int maxOccurrences = DEFAULT_MAX_OCCURRENCES;
        return new PwnedPasswordPolicyProvider(keycloakSession, maxOccurrences);
    }

    @Override
    public void init(Config.Scope scope) { }

    @Override
    public void postInit(KeycloakSessionFactory factory) { }

    @Override
    public void close() { }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayName() {
        return "Pwned Password (HaveIBeenPwned)";
    }

    @Override
    public String getConfigType() {
        return "int";
    }

    @Override
    public String getDefaultConfigValue() {
        return Integer.toString(DEFAULT_MAX_OCCURRENCES);
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }
}
