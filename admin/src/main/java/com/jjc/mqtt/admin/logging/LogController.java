package com.jjc.mqtt.admin.logging;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.util.Map;
import java.util.HashMap;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/logs")
@Tag(name = "日志管理", description = "提供日志预览和下载功能")
public class LogController {

    @Value("${logging.file.path:./logs}")
    private String logPath;

    @Value("${logging.file.name:app.log}")
    private String logFileName;

    @Operation(summary = "获取最新日志")
    @GetMapping("/recent")
    public ResponseEntity<List<String>> getRecentLogs(
            @RequestParam(defaultValue = "100") int lines,
            @RequestParam(defaultValue = "ALL") String level) {
        try {
            Path path = resolveLogPath(level);
            if (!Files.exists(path)) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            return ResponseEntity.ok(readLastLines(path, lines));
        } catch (IOException e) {
            log.error("读取日志文件失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "实时日志流")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamingResponseBody streamLogs(@RequestParam(defaultValue = "ALL") String level) {
        return outputStream -> {
            Path path = resolveLogPath(level);
            if (!Files.exists(path)) {
                outputStream.write("data: Log file not found\n\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return;
            }

            try (RandomAccessFile reader = new RandomAccessFile(path.toFile(), "r")) {
                reader.seek(reader.length());

                while (!Thread.currentThread().isInterrupted()) {
                    long currentPosition = reader.getFilePointer();
                    long fileLength = reader.length();

                    if (currentPosition < fileLength) {
                        String line = readUtf8Line(reader);
                        if (line != null) {
                            outputStream.write(("data: " + line + "\n\n").getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        }
                    } else if (currentPosition > fileLength) {
                        reader.seek(0);
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                log.error("读取日志流失败", e);
                outputStream.write(("data: Error reading log stream: " + e.getMessage() + "\n\n")
                        .getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        };
    }

    @Operation(summary = "下载日志文件")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(@RequestParam(defaultValue = "ALL") String level) {
        try {
            Path path = resolveLogPath(level);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            String fileName = path.getFileName().toString();
            FileSystemResource resource = new FileSystemResource(path.toFile());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(path.toFile().length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("下载日志文件失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "获取日志文件列表")
    @GetMapping("/files")
    public ResponseEntity<List<String>> getLogFiles() {
        try {
            Path logDir = Paths.get(logPath);
            if (!Files.exists(logDir)) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<String> files = Files.list(logDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.toList());

            return ResponseEntity.ok(files);
        } catch (IOException e) {
            log.error("获取日志文件列表失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    private List<String> readLastLines(Path path, int lines) throws IOException {
        List<String> result = new ArrayList<>();
        if (lines <= 0) {
            return result;
        }

        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            long fileLength = file.length();
            if (fileLength <= 0) {
                return result;
            }

            long position = fileLength - 1;
            long lineCount = 0;
            boolean seenContent = false;

            while (position >= 0 && lineCount < lines) {
                file.seek(position);
                int b = file.readUnsignedByte();
                if (b == '\n') {
                    if (seenContent) {
                        lineCount++;
                    }
                } else if (b != '\r') {
                    seenContent = true;
                }

                if (lineCount >= lines) {
                    position++;
                    break;
                }
                position--;
            }

            file.seek(Math.max(0, position + 1));
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Channels.newInputStream(file.getChannel()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (result.size() >= lines) {
                        result.remove(0);
                    }
                    result.add(line);
                }
            }
        }
        return result;
    }

    private String readUtf8Line(RandomAccessFile file) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = file.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                buffer.write(b);
            }
        }

        if (b == -1 && buffer.size() == 0) {
            return null;
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Operation(summary = "获取日志级别")
    @GetMapping("/level")
    public ResponseEntity<Map<String, String>> getLogLevels() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Map<String, String> levels = new HashMap<>();
        levels.put("ROOT", getLogLevelStr(context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)));
        levels.put("com.jjc.mqtt", getLogLevelStr(context.getLogger("com.jjc.mqtt")));
        levels.put("com.jjc.mqtt.admin", getLogLevelStr(context.getLogger("com.jjc.mqtt.admin")));
        return ResponseEntity.ok(levels);
    }

    @Operation(summary = "修改日志级别")
    @PostMapping("/level")
    public ResponseEntity<Map<String, Object>> updateLogLevel(
            @RequestParam String logger,
            @RequestParam String level) {
        Map<String, Object> result = new HashMap<>();
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            String loggerName = "ROOT".equalsIgnoreCase(logger) ? org.slf4j.Logger.ROOT_LOGGER_NAME : logger;
            ch.qos.logback.classic.Logger logInstance = context.getLogger(loggerName);
            if (logInstance != null) {
                logInstance.setLevel(Level.toLevel(level.toUpperCase(), Level.INFO));
                result.put("success", true);
                result.put("logger", logger);
                result.put("level", level.toUpperCase());
                result.put("message", "日志级别修改成功");
                log.info("动态调整日志级别: logger={}, level={}", loggerName, level.toUpperCase());
            } else {
                result.put("success", false);
                result.put("message", "未找到指定的日志记录器: " + logger);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "修改失败: " + e.getMessage());
            log.error("修改日志级别失败", e);
        }
        return ResponseEntity.ok(result);
    }

    private String getLogLevelStr(ch.qos.logback.classic.Logger logger) {
        if (logger == null) return "INFO";
        Level level = logger.getEffectiveLevel();
        return level != null ? level.toString() : "INFO";
    }

    private Path resolveLogPath(String level) {
        Path path = toLogPath(getLogFileNameByLevel(level));
        if (!Files.exists(path)) {
            return toLogPath(logFileName);
        }
        return path;
    }

    private Path toLogPath(String fileName) {
        Path path = Paths.get(fileName);
        if (path.isAbsolute() || path.getParent() != null) {
            return path;
        }
        return Paths.get(logPath, fileName);
    }

    private String getLogFileNameByLevel(String level) {
        return switch (level.toUpperCase()) {
            case "DEBUG" -> "debug.log";
            case "INFO" -> "info.log";
            case "WARN" -> "warn.log";
            case "ERROR" -> "error.log";
            default -> logFileName;
        };
    }
}
