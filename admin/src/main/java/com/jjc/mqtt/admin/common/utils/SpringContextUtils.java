package com.jjc.mqtt.admin.common.utils;

/**
 * @author sweeter
 * @description
 * @date 2025/12/18
 */
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@Lazy(false)
public final class SpringContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public SpringContextUtils() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static String getEnvironmentProperty(String key) {
        return getApplicationContext().getEnvironment().getProperty(key);
    }
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    public static <T> Collection<T> getBeanValuesOfType(Class<T> clazz) {
        return getBeansOfType(clazz).values();
    }
}

