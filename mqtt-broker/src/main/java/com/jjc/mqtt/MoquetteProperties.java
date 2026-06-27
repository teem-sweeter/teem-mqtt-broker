package com.jjc.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;

@ConfigurationProperties(prefix = "mqtt.broker")
public class MoquetteProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean enabled = true;
    private String host = "0.0.0.0";
    private int port = 1883;
    private int sslPort = 0;
    private int websocketPort = 8083;
    private int websocketsPort = 0;
    private String websocketPath = "/mqtt";
    private boolean allowAnonymous = false;
    private boolean persistenceEnabled = true;
    private String dataPath = "./data/mqtt-broker/";
    private String username = "admin";
    private String password = "fh";
    private DuplicateClientIdStrategy duplicateClientIdStrategy = DuplicateClientIdStrategy.REJECT_NEW;

    public MoquetteProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }

    public int getWebsocketsPort() {
        return websocketsPort;
    }

    public void setWebsocketsPort(int websocketsPort) {
        this.websocketsPort = websocketsPort;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public void setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DuplicateClientIdStrategy getDuplicateClientIdStrategy() {
        return duplicateClientIdStrategy;
    }

    public void setDuplicateClientIdStrategy(DuplicateClientIdStrategy duplicateClientIdStrategy) {
        this.duplicateClientIdStrategy = duplicateClientIdStrategy;
    }
}