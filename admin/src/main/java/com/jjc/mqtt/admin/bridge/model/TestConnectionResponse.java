package com.jjc.mqtt.admin.bridge.model;

/**
 * 测试连接响应 DTO
 *
 * @author sweeter
 */
public class TestConnectionResponse {

    private boolean success;
    private String message;
    private Long latency;

    public TestConnectionResponse() {
    }

    public TestConnectionResponse(boolean success, String message, Long latency) {
        this.success = success;
        this.message = message;
        this.latency = latency;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getLatency() { return latency; }
    public void setLatency(Long latency) { this.latency = latency; }
}
