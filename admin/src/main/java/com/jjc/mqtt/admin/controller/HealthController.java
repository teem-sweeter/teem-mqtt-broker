package com.jjc.mqtt.admin.controller;

import com.jjc.mqtt.monitor.MqttStats;
import com.jjc.mqtt.monitor.MonitorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.*;
import java.util.*;

@Tag(name = "健康检查", description = "系统健康检查相关")
@RestController
@RequestMapping("/v1/health")
public class HealthController {

    @Autowired
    private MonitorService monitorService;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());

        // JVM信息
        result.put("jvm", getJvmInfo());

        // 系统信息
        result.put("system", getSystemInfo());

        // MQTT Broker信息
        result.put("mqtt", getMqttInfo());

        // 磁盘信息
        result.put("disk", getDiskInfo());

        return result;
    }

    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        jvm.put("availableProcessors", runtime.availableProcessors());
        jvm.put("maxMemory", runtime.maxMemory());
        jvm.put("totalMemory", runtime.totalMemory());
        jvm.put("freeMemory", runtime.freeMemory());
        jvm.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());

        // 堆内存详情
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        Map<String, Long> heap = new HashMap<>();
        heap.put("init", heapUsage.getInit());
        heap.put("used", heapUsage.getUsed());
        heap.put("committed", heapUsage.getCommitted());
        heap.put("max", heapUsage.getMax());
        jvm.put("heapMemory", heap);

        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        Map<String, Long> nonHeap = new HashMap<>();
        nonHeap.put("init", nonHeapUsage.getInit());
        nonHeap.put("used", nonHeapUsage.getUsed());
        nonHeap.put("committed", nonHeapUsage.getCommitted());
        nonHeap.put("max", nonHeapUsage.getMax());
        jvm.put("nonHeapMemory", nonHeap);

        // 线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        jvm.put("threadCount", threadMXBean.getThreadCount());
        jvm.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        jvm.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        jvm.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());

        // GC信息
        List<Map<String, Object>> gcList = new ArrayList<>();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            Map<String, Object> gcInfo = new HashMap<>();
            gcInfo.put("name", gc.getName());
            gcInfo.put("collectionCount", gc.getCollectionCount());
            gcInfo.put("collectionTime", gc.getCollectionTime());
            gcList.add(gcInfo);
        }
        jvm.put("gc", gcList);

        return jvm;
    }

    private Map<String, Object> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();

        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        system.put("name", osMXBean.getName());
        system.put("version", osMXBean.getVersion());
        system.put("arch", osMXBean.getArch());
        system.put("availableProcessors", osMXBean.getAvailableProcessors());
        system.put("systemLoadAverage", osMXBean.getSystemLoadAverage());

        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean sunOs) {
            system.put("processCpuLoad", sunOs.getProcessCpuLoad());
            system.put("systemCpuLoad", sunOs.getSystemCpuLoad());
            system.put("totalPhysicalMemorySize", sunOs.getTotalPhysicalMemorySize());
            system.put("freePhysicalMemorySize", sunOs.getFreePhysicalMemorySize());
        }

        return system;
    }

    private Map<String, Object> getMqttInfo() {
        Map<String, Object> mqtt = new HashMap<>();

        MqttStats stats = monitorService.getStats();
        mqtt.put("currentConnections", stats.getCurrentConnections());
        mqtt.put("totalConnections", stats.getTotalConnections());
        mqtt.put("totalMessagesReceived", stats.getTotalMessagesReceived());
        mqtt.put("totalMessagesSent", stats.getTotalMessagesSent());
        mqtt.put("messagesPerSecond", stats.getMessagesPerSecond());
        mqtt.put("totalTopics", stats.getTotalTopics());
        mqtt.put("totalSubscriptions", stats.getTotalSubscriptions());
        mqtt.put("uptime", stats.getUptime());

        return mqtt;
    }

    private Map<String, Object> getDiskInfo() {
        Map<String, Object> disk = new HashMap<>();

        File root = new File("/");
        if (root.exists()) {
            disk.put("totalSpace", root.getTotalSpace());
            disk.put("freeSpace", root.getFreeSpace());
            disk.put("usableSpace", root.getUsableSpace());
        }

        return disk;
    }
}
