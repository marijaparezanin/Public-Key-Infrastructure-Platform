package com.ftn.pki.utils.cryptography;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AESUtils {
    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    public SecretKey generateRandomKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    public AESGcmEncrypted encrypt(SecretKey key, String plaintext) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

        return new AESGcmEncrypted(Base64.getEncoder().encodeToString(ciphertext),
                Base64.getEncoder().encodeToString(iv));
    }

    public String decrypt(SecretKey key, AESGcmEncrypted encrypted) throws Exception {
        byte[] iv = Base64.getDecoder().decode(encrypted.getIv());
        byte[] ciphertext = Base64.getDecoder().decode(encrypted.getCiphertext());

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext);
    }

    public static SecretKey secretKeyFromBase64(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decoded, AES);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AESGcmEncrypted {
        private String ciphertext; // Base64 encoded
        private String iv;         // Base64 encoded
    }
}
