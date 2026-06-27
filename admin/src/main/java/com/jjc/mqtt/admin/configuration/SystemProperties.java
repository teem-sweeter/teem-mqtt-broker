package com.jjc.mqtt.admin.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author sweeter
 * @date 2025/12/16 9:18
 * @description
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "system")
public class SystemProperties {

    @Value("${spring.application.name}")
    private String name;

    @Value("${server.port}")
    private int port;

    private String version = "v1.0.0";

    @Value("${system.temp-dir:${java.io.tmpdir}/edge_upgrade}")
    private String tempDir;
}
