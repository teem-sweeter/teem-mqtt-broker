package com.jjc.mqtt.admin.upgrade;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * @author sweeter
 * @date 2025/12/29 18:32
 * @description
 */
@Slf4j
@Service
public class PackageValidationService {

    private JarSignatureVerifier verifier;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/release-public.pem");
            if (!resource.exists()) {
                throw new IllegalStateException("Public key not found in classpath: keys/release-public.pem");
            }
            try (InputStream is = resource.getInputStream()) {
                String pemContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                String base64Key = pemContent
                        .lines()
                        .filter(line -> !line.startsWith("-"))
                        .collect(Collectors.joining());

                byte[] keyBytes = Base64.getDecoder().decode(base64Key);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                PublicKey publicKey = java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
                this.verifier = new JarSignatureVerifier(publicKey, ".bin");
                log.info("Package verifier initialized from classpath resource");
            }
        } catch (Exception e) {
            log.error("Failed to load public key", e);
            throw new IllegalStateException("Cannot initialize package signature verifier", e);
        }
    }

    /**
     * 验证Jar文件
     * @param jarFile Jar文件路径
     * @return 是否有效
     */
    public boolean isValidJar(Path jarFile) {
        if (verifier == null) {
            throw new IllegalStateException("Verifier not initialized");
        }
        try {
            return verifier.verify(jarFile);
        } catch (Exception e) {
            log.warn("Verification failed", e);
            return false;
        }
    }
}
