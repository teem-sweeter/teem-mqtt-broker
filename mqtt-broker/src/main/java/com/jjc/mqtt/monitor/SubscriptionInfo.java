package com.jjc.mqtt.monitor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订阅详情 DTO
 *
 * @author sweeter
 */
public class SubscriptionInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String topic;
    private int qos;
    private boolean noLocal;
    private boolean retainAsPublished;
    private String shareName;

    public SubscriptionInfo() {
    }

    public SubscriptionInfo(String topic, int qos, boolean noLocal, boolean retainAsPublished, String shareName) {
        this.topic = topic;
        this.qos = qos;
        this.noLocal = noLocal;
        this.retainAsPublished = retainAsPublished;
        this.shareName = shareName;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getQos() { return qos; }
    public void setQos(int qos) { this.qos = qos; }

    public boolean isNoLocal() { return noLocal; }
    public void setNoLocal(boolean noLocal) { this.noLocal = noLocal; }

    public boolean isRetainAsPublished() { return retainAsPublished; }
    public void setRetainAsPublished(boolean retainAsPublished) { this.retainAsPublished = retainAsPublished; }

    public String getShareName() { return shareName; }
    public void setShareName(String shareName) { this.shareName = shareName; }
}
