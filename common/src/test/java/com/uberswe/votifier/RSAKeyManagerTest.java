package com.uberswe.votifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RSAKeyManagerTest {

    @Test
    void generateAndLoadKeys(@TempDir Path tempDir) throws Exception {
        RSAKeyManager manager = new RSAKeyManager();
        manager.load(tempDir);

        assertNotNull(manager.getKeyPair());
        assertNotNull(manager.getKeyPair().getPublic());
        assertNotNull(manager.getKeyPair().getPrivate());
    }

    @Test
    void keyFilesCreated(@TempDir Path tempDir) throws Exception {
        RSAKeyManager manager = new RSAKeyManager();
        manager.load(tempDir);

        assertTrue(Files.exists(tempDir.resolve("public.pem")));
        assertTrue(Files.exists(tempDir.resolve("private.pem")));
    }

    @Test
    void pemFilesHaveCorrectHeaders(@TempDir Path tempDir) throws Exception {
        RSAKeyManager manager = new RSAKeyManager();
        manager.load(tempDir);

        String publicPem = Files.readString(tempDir.resolve("public.pem"));
        assertTrue(publicPem.startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(publicPem.trim().endsWith("-----END PUBLIC KEY-----"));

        String privatePem = Files.readString(tempDir.resolve("private.pem"));
        assertTrue(privatePem.startsWith("-----BEGIN PRIVATE KEY-----"));
        assertTrue(privatePem.trim().endsWith("-----END PRIVATE KEY-----"));
    }

    @Test
    void loadExistingKeysMatches(@TempDir Path tempDir) throws Exception {
        RSAKeyManager first = new RSAKeyManager();
        first.load(tempDir);
        KeyPair firstKeys = first.getKeyPair();

        RSAKeyManager second = new RSAKeyManager();
        second.load(tempDir);
        KeyPair secondKeys = second.getKeyPair();

        assertArrayEquals(firstKeys.getPublic().getEncoded(), secondKeys.getPublic().getEncoded());
        assertArrayEquals(firstKeys.getPrivate().getEncoded(), secondKeys.getPrivate().getEncoded());
    }

    @Test
    void publicKeyBase64IsValid(@TempDir Path tempDir) throws Exception {
        RSAKeyManager manager = new RSAKeyManager();
        manager.load(tempDir);

        String base64 = manager.getPublicKeyBase64();
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        assertDoesNotThrow(() -> Base64.getDecoder().decode(base64));
    }

    @Test
    void encryptDecryptRoundtrip(@TempDir Path tempDir) throws Exception {
        RSAKeyManager manager = new RSAKeyManager();
        manager.load(tempDir);

        String original = "VOTE\nTestService\nPlayerOne\n127.0.0.1\n1700000000";

        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, manager.getKeyPair().getPublic());
        byte[] encrypted = encryptCipher.doFinal(original.getBytes(StandardCharsets.UTF_8));

        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, manager.getKeyPair().getPrivate());
        byte[] decrypted = decryptCipher.doFinal(encrypted);

        assertEquals(original, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void keyIs2048Bit(@TempDir Path tempDir) throws Exception {
        RSAKeyManager manager = new RSAKeyManager();
        manager.load(tempDir);

        // RSA public key encoded length for 2048-bit key is 294 bytes
        // The modulus bit length should be 2048
        java.security.interfaces.RSAPublicKey rsaKey =
                (java.security.interfaces.RSAPublicKey) manager.getKeyPair().getPublic();
        assertEquals(2048, rsaKey.getModulus().bitLength());
    }
}
