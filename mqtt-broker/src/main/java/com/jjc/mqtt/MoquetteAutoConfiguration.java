package com.jjc.mqtt;

import com.jjc.mqtt.handler.BridgeInterceptHandler;
import com.jjc.mqtt.handler.ClientControlHandler;
import com.jjc.mqtt.handler.DefaultMoquetteHandler;
import com.jjc.mqtt.handler.MqttMonitorHandler;
import com.jjc.mqtt.handler.MqttPersistenceHandler;
import com.jjc.mqtt.monitor.MonitorService;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.interception.InterceptHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
/**
 * MQTT 消息持久化配置引导类
 * <p>
 * 使用方式：在 application.yml 或 application.properties 中配置：
 * <pre>
 * # MQTT Broker 配置
 * mqtt.broker:
 *   enabled: true
 *   host: 0.0.0.0
 *   port: 1883
 *   allow-anonymous: false
 *   username: admin
 *   password: admin123
 *
 * # MQTT 消息持久化配置
 * mqtt.broker.persistence:
 *   enabled: true                    # 是否启用消息持久化
 *   queue-size: 10000                # 异步队列大小
 *   discarding-threshold: 0          # 丢弃阈值（0表示不丢弃）
 *   log-path: ./logs/mqtt            # 日志文件目录
 *   max-file-size: 100MB             # 单文件最大大小
 *   max-history: 30                  # 保留天数
 *   total-size-cap: 20GB             # 总大小上限
 * </pre>
 *
 * MQTT Broker 自动配置类
 * 支持消息持久化处理器
 *
 * @author sweeter
 * @date 2026/2/4 12:00
 */
@Lazy(false)
@ConditionalOnProperty(prefix = "mqtt.broker", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableConfigurationProperties({MoquetteProperties.class, MqttPersistenceProperties.class})
public class MoquetteAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MoquetteAutoConfiguration.class);

    private Server mqttBroker;


    /**
     * 已连接客户端 ID 跟踪器
     */
    @Bean
    @ConditionalOnMissingBean
    public ConnectedClients connectedClients() {
        return new ConnectedClients();
    }

    /**
     * MQTT 监控服务
     */
    @Bean
    @ConditionalOnMissingBean
    public MonitorService monitorService(@Lazy Server mqttBroker) {
        return new MonitorService(mqttBroker);
    }

    /**
     * 默认消息处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public InterceptHandler defaultMoquetteHandler() {
        return new DefaultMoquetteHandler();
    }

    /**
     * MQTT 消息持久化处理器
     * 可通过配置 mqtt.persistence.enabled=false 禁用
     */
    @Bean
    @ConditionalOnProperty(prefix = "mqtt.broker.persistence", name = "enabled", havingValue = "true", matchIfMissing = true)
    public InterceptHandler mqttPersistenceHandler() {
        log.info("MQTT 消息持久化处理器已启用");
        return new MqttPersistenceHandler();
    }

    /**
     * MQTT 监控处理器
     */
    @Bean
    @ConditionalOnProperty(prefix = "mqtt.broker.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
    public InterceptHandler mqttMonitorHandler(MonitorService monitorService, ConnectedClients connectedClients) {
        log.info("MQTT 监控处理器已启用");
        return new MqttMonitorHandler(monitorService, connectedClients);
    }

    /**
     * 桥接拦截处理器
     */
    @Bean
    public InterceptHandler bridgeInterceptHandler() {
        log.info("桥接拦截处理器已启用");
        return new BridgeInterceptHandler();
    }

    /**
     * 客户端管控拦截处理器
     */
    @Bean
    public InterceptHandler clientControlHandler() {
        log.info("客户端管控拦截处理器已启用");
        return new ClientControlHandler();
    }

    /**
     * MQTT Broker 服务器
     */
    @Bean(destroyMethod = "stopServer")
    public Server mqttBroker(MoquetteProperties properties, IAuthenticator authenticator,List<InterceptHandler> userHandlers) throws IOException {
        mqttBroker = new Server();
        Properties configProps = new Properties();
        configProps.setProperty(IConfig.HOST_PROPERTY_NAME, properties.getHost());
        configProps.setProperty(IConfig.PORT_PROPERTY_NAME, String.valueOf(properties.getPort()));
        configProps.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, String.valueOf(properties.isAllowAnonymous()));

        configProps.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(properties.getWebsocketPort()));
        configProps.setProperty(IConfig.WEB_SOCKET_PATH_PROPERTY_NAME, properties.getWebsocketPath());

        // 开启持久化开关
        configProps.setProperty(IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME, Boolean.toString(properties.isPersistenceEnabled()));
        // 设置数据存储目录（如果目录不存在，Moquette 会尝试自动创建）
        String dataPath = properties.getDataPath();
        if (dataPath == null || dataPath.isEmpty()) {
            dataPath = "./moquette_data/";
        }
        configProps.setProperty(IConfig.DATA_PATH_PROPERTY_NAME, dataPath);
        // 队列类型 (可选：默认为 segmented，适合高性能)
        configProps.setProperty(IConfig.PERSISTENT_QUEUE_TYPE_PROPERTY_NAME, "segmented");
        if (properties.getSslPort() > 0) {
            configProps.setProperty(IConfig.SSL_PORT_PROPERTY_NAME, String.valueOf(properties.getSslPort()));
        }
        if (properties.getWebsocketPort() > 0) {
            configProps.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, String.valueOf(properties.getWebsocketPort()));
        }
        if (properties.getWebsocketsPort() > 0) {
            configProps.setProperty(IConfig.WSS_PORT_PROPERTY_NAME, String.valueOf(properties.getWebsocketsPort()));
        }
        log.info("启动 MQTT Broker: host={}, port={}, dataPath={}", properties.getHost(), properties.getPort(), dataPath);
        mqttBroker.startServer(new MemoryConfig(configProps), userHandlers, null, authenticator, null);
        return mqttBroker;
    }

    /**
     * 认证器
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultMoquetteAuthenticator authenticator(MoquetteProperties properties,
                                                      ObjectProvider<ConnectedClients> connectedClientsProvider,
                                                      ObjectProvider<MonitorService> monitorServiceProvider) {
        return new DefaultMoquetteAuthenticator(properties.getUsername(), properties.getPassword(),
                properties.isAllowAnonymous(), properties.getDuplicateClientIdStrategy(),
                connectedClientsProvider, monitorServiceProvider);
    }
}
