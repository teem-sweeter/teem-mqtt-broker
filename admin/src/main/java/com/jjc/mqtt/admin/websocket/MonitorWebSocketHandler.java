package com.jjc.mqtt.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjc.mqtt.monitor.MqttStats;
import com.jjc.mqtt.monitor.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class MonitorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MonitorWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MonitorService monitorService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket 连接已建立: sessionId={}", session.getId());
        sendStats(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: sessionId={}, payload={}", session.getId(), payload);

        try {
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            String type = (String) request.get("type");

            if ("GET_STATS".equals(type)) {
                sendStats(session);
            } else if ("GET_CLIENTS".equals(type)) {
                sendClients(session);
            } else if ("GET_MESSAGES".equals(type)) {
                int limit = request.containsKey("limit") ?
                        ((Number) request.get("limit")).intValue() : 100;
                sendRecentMessages(session, limit);
            } else if ("DISCONNECT_CLIENT".equals(type)) {
                String clientId = (String) request.get("clientId");
                handleDisconnectClient(session, clientId);
            } else if ("PING".equals(type)) {
                session.sendMessage(new TextMessage(
                        objectMapper.writeValueAsString(Map.of("type", "PONG", "timestamp", System.currentTimeMillis()))));
            } else {
                log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("type", "ERROR", "message", "消息处理失败"))));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket 连接已关闭: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误: sessionId={}", session.getId(), exception);
        sessions.remove(session);
    }

    @EventListener
    public void handleMonitorEvent(MonitorService.MonitorEvent event) {
        broadcastEvent(event);
    }

    @Scheduled(fixedRate = 1000)
    public void sendPeriodicStats() {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            MqttStats stats = monitorService.getStats();
            String message = objectMapper.writeValueAsString(Map.of(
                    "type", "STATS_UPDATE",
                    "data", stats,
                    "timestamp", System.currentTimeMillis()
            ));

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error("发送 WebSocket 消息失败: sessionId={}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送定期统计信息失败", e);
        }
    }

    private void sendStats(WebSocketSession session) throws IOException {
        MqttStats stats = monitorService.getStats();
        String message = objectMapper.writeValueAsString(Map.of(
                "type", "STATS",
                "data", stats,
                "timestamp", System.currentTimeMillis()
        ));
        session.sendMessage(new TextMessage(message));
    }

    private void sendClients(WebSocketSession session) throws IOException {
        List<?> clients = monitorService.getConnectedClients();
        String message = objectMapper.writeValueAsString(Map.of(
                "type", "CLIENTS",
                "data", clients,
                "timestamp", System.currentTimeMillis()
        ));
        session.sendMessage(new TextMessage(message));
    }

    private void sendRecentMessages(WebSocketSession session, int limit) throws IOException {
        List<?> messages = monitorService.getRecentMessages(limit);
        String message = objectMapper.writeValueAsString(Map.of(
                "type", "MESSAGES",
                "data", messages,
                "timestamp", System.currentTimeMillis()
        ));
        session.sendMessage(new TextMessage(message));
    }

    private void handleDisconnectClient(WebSocketSession session, String clientId) throws IOException {
        boolean success = monitorService.disconnectClient(clientId);
        String responseMessage = objectMapper.writeValueAsString(Map.of(
                "type", "DISCONNECT_RESULT",
                "success", success,
                "clientId", clientId,
                "timestamp", System.currentTimeMillis()
        ));
        session.sendMessage(new TextMessage(responseMessage));
    }

    private void broadcastEvent(MonitorService.MonitorEvent event) {
        if (sessions.isEmpty()) {
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "type", event.getType(),
                    "data", event.getData(),
                    "timestamp", event.getTimestamp()
            ));

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error("广播消息失败: sessionId={}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("广播事件失败", e);
        }
    }

    public int getActiveSessionCount() {
        return (int) sessions.stream().filter(WebSocketSession::isOpen).count();
    }
}