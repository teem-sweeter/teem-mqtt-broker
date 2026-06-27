package com.jjc.mqtt.admin.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Edge Monitor API")
                        .version("1.0")
                        .description("Edge Monitor 接口文档"));
    }

    @Bean
    public GroupedOpenApi mainApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/v1/**","/health","/health/*")
                .build();
    }
}
