package com.jjc.mqtt.admin.actuator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * @author sweeter
 * @date 2025/12/22 15:58
 * @description
 */
@Slf4j
@Component
@WebEndpoint(id = "restart")
public class RestartEndpoint {
    public static final EndpointId ID = EndpointId.of("restart");

    /**
     * 重启服务
     * @return 重启结果
     */
    @WriteOperation
    public String restart() {
        new Thread(() -> {
            try {
                // 等待HTTP响应返回
                Thread.sleep(1000);
                log.info("开始重启当前应用程序...");
                // 检查是否存在启动脚本
                String osName = System.getProperty("os.name").toLowerCase();
                String scriptName = osName.contains("win") ? "endpoint-start.cmd" : "endpoint-start.sh";
                String CLASS_PATH = System.getProperty("java.class.path");
                String JAVA_HOME = System.getProperty("java.home");
                String commandLine = System.getProperty("sun.java.command");
                String DIR = System.getProperty("user.dir");
                File scriptFile = new File(DIR, scriptName);
                //构建一个启动脚本
                String script;
                if (osName.contains("win")) {
                    //Windows 版本的启动脚本
                    script = "@echo off\n" +
                            "cd " + DIR + "\n" +
                            "start " + JAVA_HOME + "/bin/java -jar "  + commandLine;
                }else {
                    //Linux 版本的启动脚本
                    script= "#!/bin/bash\n" +
                            "cd " + DIR + "\n" +
                            "nohup " + JAVA_HOME + "/bin/java -jar "  + commandLine + " > /dev/null 2>&1 &";
                }
                //写入到DIR
                try (FileWriter writer = new FileWriter(scriptFile)) {
                    writer.write(script);
                    writer.flush();
                    // 设置脚本权限
                    scriptFile.setExecutable(true, true);
                }
                //执行脚本
                String command = String.format(
                        "nohup %s  </dev/null >/dev/null 2>&1 &",
                        scriptFile
                );
                // 启动新进程
                ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
                pb.redirectErrorStream(true);
                pb.directory(new File(DIR));
                Process process = pb.start();
                ProcessHandle.Info info = process.info();
                log.info("PID: {}", process.pid());
                log.info("命令: {}", info.command().orElse("N/A"));
                log.info("命令行: {}", info.commandLine().orElse("N/A"));
                log.info("用户: {}", info.user().orElse("N/A"));
                info.startInstant().ifPresent(inst ->
                        log.info("启动时间: {}", inst.atZone(ZoneId.systemDefault()).toLocalDateTime())
                );
                // 等待脚本启动
                boolean exited = process.waitFor(2, TimeUnit.SECONDS);
                if (exited) {
                    log.warn("Update script exited immediately with code: {}", process.exitValue());
                }
                Thread.sleep(1000);
                // 退出当前进程
                System.exit(0);
            } catch (Exception e) {
                log.error("重启失败", e);
            }
        }, "RestartThread").start();

        return "Restart initiated. Application will shutdown in 3 seconds.";
    }
}
