package com.jjc.ui;

/**
 * @author sweeter
 * @date 2025/12/10 16:46
 * @description
 */
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebjarsAutoConfiguration {
    @Configuration
    public static class WebjarsResourceHandler implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/ui/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/")
                    .resourceChain(false);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RootRedirectController rootRedirectController() {
        return new RootRedirectController();
    }
}
