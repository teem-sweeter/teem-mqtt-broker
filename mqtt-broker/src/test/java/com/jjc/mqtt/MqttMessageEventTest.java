package com.jjc.mqtt;

import com.jjc.mqtt.event.MqttMessageEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MqttMessageEventTest {

    @Test
    void toJsonLine_shouldProduceValidJson() {
        MqttMessageEvent event = new MqttMessageEvent("sensor/temp", "25.5", 1700000000000L, 1, "client-1");

        String json = event.toJsonLine();

        assertTrue(json.contains("\"topic\":\"sensor/temp\""));
        assertTrue(json.contains("\"qos\":1"));
        assertTrue(json.contains("\"clientId\":\"client-1\""));
        assertTrue(json.contains("\"ts\":1700000000000"));
    }

    @Test
    void toJsonLine_shouldEscapeSpecialChars() {
        MqttMessageEvent event = new MqttMessageEvent("topic/\"test\"", "raw-payload", 1700000000000L, 0, "c1");

        String json = event.toJsonLine();

        assertTrue(json.contains("topic/\\\"test\\\""));
        assertTrue(json.contains("raw-payload"));
    }

    @Test
    void gettersAndSetters_shouldWork() {
        MqttMessageEvent event = new MqttMessageEvent();
        event.setTopic("test/topic");
        event.setPayload("{\"key\":\"value\"}");
        event.setTimestamp(1700000000000L);
        event.setQos(2);
        event.setClientId("client-001");

        assertEquals("test/topic", event.getTopic());
        assertEquals("{\"key\":\"value\"}", event.getPayload());
        assertEquals(1700000000000L, event.getTimestamp());
        assertEquals(2, event.getQos());
        assertEquals("client-001", event.getClientId());
    }
}
