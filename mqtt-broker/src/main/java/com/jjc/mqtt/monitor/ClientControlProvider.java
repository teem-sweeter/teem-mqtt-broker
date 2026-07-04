package com.jjc.mqtt.monitor;

public interface ClientControlProvider {
    boolean isSendDisabled(String clientId);
    boolean isReceiveDisabled(String clientId);
}
