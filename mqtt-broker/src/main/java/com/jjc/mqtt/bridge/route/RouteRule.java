package com.jjc.mqtt.bridge.route;

import java.io.Serial;
import java.io.Serializable;

/**
 * 桥接路由规则
 *
 * @author sweeter
 */
public class RouteRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String direction;      // outbound / inbound
    private String sourceTopic;    // 源主题
    private String destTopic;      // 目的主题
    private int qos = -1;          // -1 表示透传
    private String retainHandling; // keep / strip / ifRetained

    public RouteRule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSourceTopic() {
        return sourceTopic;
    }

    public void setSourceTopic(String sourceTopic) {
        this.sourceTopic = sourceTopic;
    }

    public String getDestTopic() {
        return destTopic;
    }

    public void setDestTopic(String destTopic) {
        this.destTopic = destTopic;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getRetainHandling() {
        return retainHandling;
    }

    public void setRetainHandling(String retainHandling) {
        this.retainHandling = retainHandling;
    }
}
