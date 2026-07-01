package com.jjc.mqtt.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = {"com.jjc.mqtt.admin.persistence.repository", "com.jjc.mqtt.admin.bridge.repository", "com.jjc.mqtt.admin.client.repository", "com.jjc.mqtt.admin.delayed.repository", "com.jjc.mqtt.admin.webhook.repository"})
@EnableTransactionManagement
public class Application {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.setApplicationStartup(new BufferingApplicationStartup(2048));
        springApplication.setLazyInitialization(true);
        springApplication.addListeners(new ApplicationPidFileWriter("app.pid"));
        ConfigurableApplicationContext applicationContext = springApplication.run(args);
        {
            Environment env = applicationContext.getEnvironment();
            String name = env.getProperty("spring.application.name");
            String port = env.getProperty("server.port");
            System.out.println("应用名称:    " + name);
            System.out.println("port:       " + port);
            System.out.println("Local:      http://localhost:" + port);
            getAllLocalHost().stream().map(v -> "Network:    http://" + v + ":" + port).forEach(System.out::println);
        }
    }


    private static List<String> getAllLocalHost() {
        List<String> hosts = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress()  && address.getHostAddress().indexOf(':') == -1) {
                        hosts.add(address.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hosts;
    }
}
