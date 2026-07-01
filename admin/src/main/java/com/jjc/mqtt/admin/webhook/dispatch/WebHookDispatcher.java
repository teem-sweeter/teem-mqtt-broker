package com.jjc.mqtt.admin.webhook.dispatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjc.mqtt.admin.webhook.entity.WebHookEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebHookDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebHookDispatcher.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient;

    public WebHookDispatcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void enqueue(WebHookEntity wh, String topic, byte[] payload, String clientId, int qos, boolean retained) {
        dispatch(wh, topic, payload, clientId, qos, retained);
    }

    @Async
    protected void dispatch(WebHookEntity wh, String topic, byte[] payload, String clientId, int qos, boolean retained) {
        String bodyStr = buildBody(topic, payload, clientId, qos, retained);
        int maxRetries = wh.getRetryCount() != null ? wh.getRetryCount() : 0;
        int retryInterval = wh.getRetryInterval() != null ? wh.getRetryInterval() : 5;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(wh.getUrl()))
                        .timeout(Duration.ofSeconds(wh.getReadTimeout() != null ? wh.getReadTimeout() : 30))
                        .header("Content-Type", wh.getContentType() != null ? wh.getContentType() : "application/json");

                if (wh.getHeaders() != null && !wh.getHeaders().isEmpty()) {
                    parseHeaders(wh.getHeaders()).forEach(builder::header);
                }

                builder.POST(HttpRequest.BodyPublishers.ofString(bodyStr, StandardCharsets.UTF_8));

                HttpResponse<String> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    log.debug("WebHook dispatch success: webhook={}, topic={}, status={}", wh.getName(), topic, resp.statusCode());
                    return;
                }
                log.warn("WebHook dispatch non-success: webhook={}, topic={}, status={}, body={}",
                        wh.getName(), topic, resp.statusCode(), truncate(resp.body(), 200));
            } catch (Exception e) {
                log.warn("WebHook dispatch failed: webhook={}, topic={}, attempt={}/{}: {}",
                        wh.getName(), topic, attempt + 1, maxRetries + 1, e.getMessage());
            }

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryInterval * 1000L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private String buildBody(String topic, byte[] payload, String clientId, int qos, boolean retained) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("topic", topic);
            body.put("payload", new String(payload, StandardCharsets.UTF_8));
            body.put("payloadBase64", Base64.getEncoder().encodeToString(payload));
            body.put("clientId", clientId);
            body.put("qos", qos);
            body.put("retained", retained);
            body.put("timestamp", System.currentTimeMillis());
            return mapper.writeValueAsString(body);
        } catch (Exception e) {
            log.warn("Failed to serialize WebHook body", e);
            return "{}";
        }
    }

    private Map<String, String> parseHeaders(String headersStr) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headersStr == null || headersStr.isEmpty()) return result;
        String[] lines = headersStr.split("\\n");
        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                if (!key.isEmpty()) {
                    result.put(key, val);
                }
            }
        }
        return result;
    }

    private String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
