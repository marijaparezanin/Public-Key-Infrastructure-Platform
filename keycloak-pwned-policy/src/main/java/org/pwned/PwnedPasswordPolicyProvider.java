package org.pwned;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PolicyError;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class PwnedPasswordPolicyProvider implements PasswordPolicyProvider {

    private final int maxOccurrences;
    private final KeycloakSession session;

    public PwnedPasswordPolicyProvider(KeycloakSession session, int maxOccurrences) {
        this.session = session;
        this.maxOccurrences = maxOccurrences;
    }

    @Override
    public void close() {
        // nothing to clean up
    }

    private static String toSha1Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private int queryPwnedCount(String sha1Upper) throws Exception {
        String prefix = sha1Upper.substring(0, 5);
        String suffix = sha1Upper.substring(5);
        String urlStr = "https://api.pwnedpasswords.com/range/" + prefix;

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Keycloak-PwnedPolicy/1.0");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() != 200) {
            return 0; // fail-open
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length != 2) continue;
                if (parts[0].equalsIgnoreCase(suffix)) {
                    return Integer.parseInt(parts[1].trim());
                }
            }
        }
        return 0;
    }

    // Main validate method for full context (user + realm)
    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        try {
            String sha1 = toSha1Hex(password);
            int count = queryPwnedCount(sha1);
            if (count > maxOccurrences) {
                return new PolicyError("passwordIsPwned", Integer.toString(count));
            }
        } catch (Exception e) {
            System.err.println("Pwned check failed: " + e.getMessage());
        }
        return null; // OK
    }

    // Additional methods required in 26.x
    @Override
    public PolicyError validate(String password, String realmId) {
        // Simplified version just calls main validate method
        return validate(null, null, password);
    }

    @Override
    public Object parseConfig(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // default
        }
    }


}
