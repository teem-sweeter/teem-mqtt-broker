package com.jjc.mqtt.admin.upgrade;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Stream;

/**
 * @author sweeter
 * @date 2025/12/29 18:32
 * @description JAR 文件数字签名验证工具类
 *   <p>支持使用 RSA 公钥验证 JAR 文件的 SHA256withRSA 签名。签名文件默认为 JAR 文件同名 + .key（可自定义后缀）。
 */
public class JarSignatureVerifier {
    private final PublicKey publicKey;
    private final String signatureSuffix;

    /**
     * 构造器：使用 PEM 格式的公钥文件和默认后缀 (.key)
     */
    public JarSignatureVerifier(Path publicKeyPemFile) throws Exception {
        this(publicKeyPemFile, ".key");
    }

    /**
     * 构造器：使用 PEM 格式的公钥文件和自定义签名后缀
     */
    public JarSignatureVerifier(Path publicKeyPemFile, String signatureSuffix) throws Exception {
        this.publicKey = loadPublicKey(publicKeyPemFile);
        this.signatureSuffix = signatureSuffix;
    }

    /**
     * 构造器：直接传入 PublicKey 对象（适用于公钥已加载的场景）
     */
    public JarSignatureVerifier(PublicKey publicKey, String signatureSuffix) {
        this.publicKey = publicKey;
        this.signatureSuffix = signatureSuffix;
    }

    /**
     * 验证 JAR 文件的签名是否有效
     *
     * @param jarFile JAR 文件路径
     * @return true 如果签名有效且 JAR 未被篡改
     * @throws Exception 文件读取或验证过程中的任何异常
     */
    public boolean verify(Path jarFile) throws Exception {
        if (!Files.exists(jarFile)) {
            throw new IllegalArgumentException("JAR file not found: " + jarFile);
        }

        Path sigFile = resolveSignatureFile(jarFile);
        if (!Files.exists(sigFile)) {
            throw new IllegalArgumentException("Signature file not found: " + sigFile);
        }

        return verifySignature(jarFile, sigFile, publicKey);
    }

    /**
     * 获取对应 JAR 的签名文件路径（根据后缀规则）
     */
    public Path resolveSignatureFile(Path jarFile) {
        String jarName = jarFile.getFileName().toString();
        if (!jarName.endsWith(".jar")) {
            throw new IllegalArgumentException("File is not a JAR: " + jarName);
        }
        String baseName = jarName.substring(0, jarName.length() - 4);
        String sigName = baseName + signatureSuffix;
        return jarFile.resolveSibling(sigName);
    }

    /**
     *   加载 PEM 格式的公钥
     * @param pemFile  PEM 文件路径
     * @return  PublicKey
     * @throws Exception
     */
    private static PublicKey loadPublicKey(Path pemFile) throws Exception {
        try (Stream<String> lines = Files.lines(pemFile)) {
            String key = lines
                    .filter(line -> !line.startsWith("-----"))
                    .reduce("", String::concat);
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }

    private static boolean verifySignature(Path jarFile, Path sigFile, PublicKey publicKey) throws Exception {
        byte[] jarBytes = Files.readAllBytes(jarFile);
        byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(jarBytes);
        byte[] signature = Files.readAllBytes(sigFile);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(hash);
        return sig.verify(signature);
    }
}
