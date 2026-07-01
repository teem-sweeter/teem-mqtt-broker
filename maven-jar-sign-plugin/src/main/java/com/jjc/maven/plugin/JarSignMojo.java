package com.jjc.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Stream;

@Mojo(name = "sign-jar", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class JarSignMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private String buildDirectory;

    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    private String finalName;

    @Parameter(property = "sign.privateKey", required = true)
    private String privateKeyPath;

    @Parameter(property = "sign.name")
    private String name;

    @Parameter(property = "sign.extend", defaultValue = ".key")
    private String extend;

    @Parameter(property = "sign.outputZip", defaultValue = "true")
    private boolean outputZip;

    @Parameter(property = "sign.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("JAR signing skipped (sign.skip=true)");
            return;
        }
        try {
            // 1. 确定 JAR 文件名
            if (name == null || name.isEmpty()) {
                name = finalName + ".jar";
            }
            // 2. 确保扩展名以 . 开头
            if (extend == null || extend.isEmpty() || !extend.startsWith(".")) {
                extend = ".key";
            }
            Path jarFile = Paths.get(buildDirectory, name);
            Path sigFile = Paths.get(buildDirectory, name.replace(".jar", extend));
            if (!Files.exists(jarFile)) {
                throw new MojoExecutionException("JAR file not found: " + jarFile);
            }
            getLog().info("Signing JAR: " + jarFile);
            // 3. 加载私钥并签名
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            byte[] jarBytes = Files.readAllBytes(jarFile);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(jarBytes);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(hash);
            byte[] signature = sig.sign();
            Files.write(sigFile, signature);
            getLog().info("Signature written to: " + sigFile);

            // 4.打包为 ZIP
            if (outputZip) {
                String zipFileName = finalName + ".zip";
                Path zipFile = Paths.get(buildDirectory, zipFileName);
                createZipWithJarAndSig(jarFile, sigFile, zipFile);
                getLog().info("ZIP package created: " + zipFile);
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to sign JAR or create ZIP", e);
        }
    }

    private void createZipWithJarAndSig(Path jarFile, Path sigFile, Path zipFile) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            addFileToZip(zipOut, jarFile);
            addFileToZip(zipOut, sigFile);
        }
        getLog().info("ZIP package created: "+ zipFile.getFileName().toString());
    }

    private void addFileToZip(ZipOutputStream zipOut, Path file) throws IOException {
        ZipEntry entry = new ZipEntry(file.getFileName().toString());
        entry.setTime(Files.getLastModifiedTime(file).toMillis());
        zipOut.putNextEntry(entry);
        Files.copy(file, zipOut);
        zipOut.closeEntry();
    }

    private PrivateKey loadPrivateKey(String pemPath) throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get(pemPath))) {
            String key = lines
                    .filter(line -> !line.startsWith("-"))
                    .collect(java.util.stream.Collectors.joining());
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }
}
