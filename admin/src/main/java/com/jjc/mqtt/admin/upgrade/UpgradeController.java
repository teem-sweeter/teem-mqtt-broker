package com.jjc.mqtt.admin.upgrade;

import com.jjc.mqtt.admin.configuration.SystemProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

/**
 * @author sweeter
 * @date 2025/12/19 16:26
 * @description
 */

@Tag(name = "系统升级", description = "通过上传程序包实现系统更新")
@RequestMapping("/v1/upgrade")
@Slf4j
@RestController
public class UpgradeController {

    private final SystemProperties systemProperties;
    private final PackageValidationService packageValidationService;

    public UpgradeController(SystemProperties systemProperties, PackageValidationService packageValidationService) {
        this.systemProperties = systemProperties;
        this.packageValidationService = packageValidationService;
    }

    // 获取当前运行的JAR文件所在目录
    private static String getCurrentJarDirectory() {
        try {
            return System.getProperty("user.dir");
        } catch (Exception e) {
            log.error("Failed to get current JAR directory", e);
        }
        return Paths.get("").toString();
    }



    // 脚本在运行时的实际路径，由启动时复制决定
    private String runtimeScriptPath;

    @PostConstruct
    public void init() {
        this.copyUpgradeScript();
        this.cleanUpgradeDirectory();
    }

    private void copyUpgradeScript() {
        String jarDir = getCurrentJarDirectory();
        runtimeScriptPath = Paths.get(jarDir, "upgrade.sh").toString();
        ClassPathResource resource = new ClassPathResource("upgrade.sh");
        if (resource.exists()) {
            try (InputStream inputStream = resource.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(runtimeScriptPath)) {
                inputStream.transferTo(outputStream);
                File scriptFile = new File(runtimeScriptPath);
                // 设置为可执行
                scriptFile.setExecutable(true, true);
                log.info("Startup script copied and made executable: {}", runtimeScriptPath);
            } catch (IOException e) {
                log.error("Failed to copy startup script from resources to {}: {}", runtimeScriptPath, e.getMessage(), e);
            }
        } else {
            log.warn("Startup script 'upgrade.sh' not found in classpath resources.");
            // 如果脚本不存在，可以设置为null或抛出异常，取决于业务逻辑
            runtimeScriptPath = null;
        }
    }

    private void cleanUpgradeDirectory() {
        String tempDir = systemProperties.getTempDir();
        Path tempDirectory = Paths.get(tempDir);
        if (!tempDirectory.toFile().exists()) {
            return;
        }
        //清空临时目录的jar文件
        try(Stream<Path> paths = Files.walk(tempDirectory)) {
            paths.map(Path::toFile)
                    .filter(File::isFile)
                    .forEach(file -> {
                        try {
                            file.delete();
                        } catch (Exception e) {
                            log.error("Failed to delete file: {}", file.getAbsolutePath(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to walk through temp directory: {}", tempDirectory, e);
        }
    }


    @Operation(summary = "上传升级包")
    @PostMapping()
    public ResponseEntity<String> uploadUpdate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (runtimeScriptPath == null || !new File(runtimeScriptPath).exists()) {
            return ResponseEntity.status(500).body("Restart script is missing or not accessible. Cannot proceed with update.");
        }
        String tempDir = systemProperties.getTempDir();
        Path tempDirectory = Paths.get(tempDir);
        try {
            Files.createDirectories(tempDirectory);
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".zip")) {
                return ResponseEntity.badRequest().body("Invalid file format. Only .zip files are allowed.");
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uniqueFilename = UUID.randomUUID() + "_" + timestamp + ".zip";
            Path tempFilePath = tempDirectory.resolve(uniqueFilename);
            // 保存上传的文件
            file.transferTo(tempFilePath.toFile());
            log.info("Update file uploaded to: {}", tempFilePath);
            Path extractedDir = tempDirectory.resolve("extracted_" + UUID.randomUUID());
            Files.createDirectories(extractedDir);
            unzip(tempFilePath, extractedDir);
            Path jarFile = Files.walk(extractedDir)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No JAR file found in ZIP"));

            if (packageValidationService.isValidJar(jarFile)) {
                log.info("JAR file is valid.");
            }else {
                //清理文件
                cleanup(extractedDir, tempFilePath);
                return ResponseEntity.status(500).body("Invalid JAR file. Cannot proceed with update.");
            }
            // 执行java主程序更新
            this.performUpdate(getCurrentJarDirectory(), jarFile.toString());
            return ResponseEntity.ok("Update uploaded and process initiated. Application will restart.");
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            return ResponseEntity.status(500).body("Failed to save file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during update", e);
            return ResponseEntity.status(500).body("Update failed: " + e.getMessage());
        }
    }


    /**
     * 递归复制目录
     */
    private void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walkFileTree(sourceDir, new java.nio.file.SimpleFileVisitor<Path>() {
            @Override
            public java.nio.file.FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetSubDir = targetDir.resolve(sourceDir.relativize(dir));
                Files.createDirectories(targetSubDir);
                return java.nio.file.FileVisitResult.CONTINUE;
            }

            @Override
            public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                Path targetFile = targetDir.resolve(sourceDir.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

    private void performUpdate(String currentJarDir, String newJarPath) throws IOException {
        String currentJarName = getCurrentJarFileName();
        String currentJarFullPath = Paths.get(currentJarDir, currentJarName).toString();
        String backupPath = currentJarFullPath + ".backup";
        // 1. 备份当前JAR
        Files.copy(Paths.get(currentJarFullPath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
        log.info("Backup created at: {}", backupPath);
        String command = String.format(
                "nohup %s %s %s </dev/null >/dev/null 2>&1 &",
                runtimeScriptPath,
                currentJarName,
                newJarPath
        );
        // 3. 启动后台线程执行脚本并退出
        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
                processBuilder.directory(new File(currentJarDir));
                processBuilder.redirectErrorStream(true);
                log.info("Executing update script: {}", command);
                Process process = processBuilder.start();
                //读取输出（但已重定向到 /dev/null，通常为空）
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[Script] {}", line);
                    }
                }
                // 等待脚本启动（短暂）
                boolean exited = process.waitFor(2, TimeUnit.SECONDS);
                if (exited) {
                    log.warn("Update script exited immediately with code: {}", process.exitValue());
                }
                // 延迟退出，确保脚本已 fork 新进程
                Thread.sleep(1000);
                // 安全退出当前 JVM
                log.info("Shutting down current instance to complete update...");
                System.exit(0);
            } catch (Exception e) {
                log.error("Failed to perform update and restart", e);
            }
        }, "UpdateAndExitThread").start();
        log.info("Update process launched in background. Method returns now.");
    }

    private static String getCurrentJarFileName() {
        try {
            File file = new File(Objects.requireNonNull(getJarPath(UpgradeController.class)));
            log.info("Current JAR path: {}", file.getAbsolutePath());
            log.info("Current JAR file name: {}", file.getName());
            return file.getName();
        } catch (Exception e) {
            log.warn("Failed to get current JAR file name", e);
        }
        //从系统环境变量java.class.path中获取
        String classPath = System.getProperty("java.class.path");
        log.info("Current JAR file name from java.class.path: {}", classPath);
        return classPath;
    }
    public static String getJarPath(Class<?> clazz) {
        String url = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
        int firstSlash = url.indexOf('/');
        if (firstSlash == -1) return null;
        int jarEnd = url.indexOf(".jar", firstSlash);
        if (jarEnd == -1) {
           throw new RuntimeException("Not a jar file: " + url);
        }
        String path = url.substring(firstSlash, jarEnd + 4);
        return java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
    }

    private void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();
                if (!entryPath.startsWith(targetDir)) {
                    throw new IOException("Zip Slip vulnerability detected!");
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    /**
     * 清理文件
     * @param paths 需要清理的文件路径
     */
    private void cleanup(Path... paths) {
        for (Path path : paths) {
            try {
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        try (Stream<Path> stream = Files.walk(path)) {
                            stream.sorted((a, b) -> -a.compareTo(b)).forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException ignored) {
                                }
                            });
                        }
                    } else {
                        Files.delete(path);
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to clean up: {}", path, e);
            }
        }
    }
}
