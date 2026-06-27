package com.jjc.mqtt.admin.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author sweeter
 * @date 2025/12/22 13:11
 * @description 自定义 Actuator Info 贡献者，提供启动时间等信息
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> customInfo = new LinkedHashMap<>();
        customInfo.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ProcessHandle.Info info = ProcessHandle.current().info();
        customInfo.put("pid", ProcessHandle.current().pid());
        info.startInstant().ifPresent(inst ->
                customInfo.put("startTime", inst.atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        );
        customInfo.forEach(builder::withDetail);
    }
}
