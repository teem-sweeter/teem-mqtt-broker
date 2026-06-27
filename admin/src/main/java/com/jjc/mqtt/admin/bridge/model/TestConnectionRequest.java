package com.jjc.mqtt.admin.bridge.model;

/**
 * 测试连接请求 DTO
 *
 * @author sweeter
 */
public class TestConnectionRequest {

    private String remoteUrl;
    private String clientId;
    private String authType;
    private String username;
    private String password;
    private String caCert;
    private String clientCert;
    private String clientKey;
    private Integer connectionTimeout = 10;

    public TestConnectionRequest() {
    }

    public String getRemoteUrl() { return remoteUrl; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCaCert() { return caCert; }
    public void setCaCert(String caCert) { this.caCert = caCert; }

    public String getClientCert() { return clientCert; }
    public void setClientCert(String clientCert) { this.clientCert = clientCert; }

    public String getClientKey() { return clientKey; }
    public void setClientKey(String clientKey) { this.clientKey = clientKey; }

    public Integer getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Integer connectionTimeout) { this.connectionTimeout = connectionTimeout; }
}
