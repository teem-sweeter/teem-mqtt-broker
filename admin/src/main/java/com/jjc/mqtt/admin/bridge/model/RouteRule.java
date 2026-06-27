package com.jjc.mqtt.admin.bridge.model;

/**
 * 路由规则 DTO
 *
 * @author sweeter
 */
public class RouteRule {

    private Long id;
    private String direction;
    private String sourceTopic;
    private String destTopic;
    private Integer qos;
    private String retainHandling;
    private Integer sortOrder;

    public RouteRule() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getSourceTopic() { return sourceTopic; }
    public void setSourceTopic(String sourceTopic) { this.sourceTopic = sourceTopic; }

    public String getDestTopic() { return destTopic; }
    public void setDestTopic(String destTopic) { this.destTopic = destTopic; }

    public Integer getQos() { return qos; }
    public void setQos(Integer qos) { this.qos = qos; }

    public String getRetainHandling() { return retainHandling; }
    public void setRetainHandling(String retainHandling) { this.retainHandling = retainHandling; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
