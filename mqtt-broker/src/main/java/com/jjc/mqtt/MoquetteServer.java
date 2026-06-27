package com.jjc.mqtt;

import com.jjc.mqtt.handler.MqttMonitorHandler;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.interception.InterceptHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * 自定义 MQTT Server，支持获取客户端 IP 地址
 *
 * 通过覆盖 startServer 方法，在创建 NewNettyMQTTHandler 时使用自定义版本
 */
public class MoquetteServer extends Server {


}