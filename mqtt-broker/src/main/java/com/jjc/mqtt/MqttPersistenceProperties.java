package com.jjc.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;

@ConfigurationProperties(prefix = "mqtt.broker.persistence")
public class MqttPersistenceProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean enabled = true;
    private int queueSize = 10000;
    private int discardingThreshold = 0;
    private String logPath = "./logs/mqtt";
    private String maxFileSize = "100MB";
    private int maxHistory = 30;
    private String totalSizeCap = "20GB";
    private int maxRows = 10000;

    public MqttPersistenceProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public String getTotalSizeCap() {
        return totalSizeCap;
    }

    public void setTotalSizeCap(String totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
}