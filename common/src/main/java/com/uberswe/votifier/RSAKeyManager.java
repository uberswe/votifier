package com.uberswe.votifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyManager {
    private KeyPair keyPair;

    public void load(Path configDir) throws Exception {
        Path publicKeyFile = configDir.resolve("public.pem");
        Path privateKeyFile = configDir.resolve("private.pem");

        if (Files.exists(publicKeyFile) && Files.exists(privateKeyFile)) {
            String publicKeyPem = Files.readString(publicKeyFile);
            String privateKeyPem = Files.readString(privateKeyFile);

            byte[] publicKeyBytes = Base64.getDecoder().decode(
                    publicKeyPem
                            .replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s", "")
            );
            byte[] privateKeyBytes = Base64.getDecoder().decode(
                    privateKeyPem
                            .replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s", "")
            );

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            keyPair = new KeyPair(publicKey, privateKey);
        } else {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            keyPair = kpg.generateKeyPair();

            Files.createDirectories(configDir);

            String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(76, "\n".getBytes()).encodeToString(keyPair.getPublic().getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getMimeEncoder(76, "\n".getBytes()).encodeToString(keyPair.getPrivate().getEncoded())
                    + "\n-----END PRIVATE KEY-----\n";

            Files.writeString(publicKeyFile, publicKeyPem);
            Files.writeString(privateKeyFile, privateKeyPem);
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}
