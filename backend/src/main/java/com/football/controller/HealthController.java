package com.football.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查端点
 * 用途：运维人员快速验证服务状态（不依赖数据库，仅返回应用层信息）
 * 访问: GET /api/health
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 健康检查
     * 返回应用基本信息、运行状态和当前时间
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("service", "football-backend");
        result.put("profile", activeProfile);
        result.put("java", System.getProperty("java.version"));
        result.put("time", LocalDateTime.now().format(FORMATTER));
        result.put("uptime", getUptime());

        return ResponseEntity.ok(result);
    }

    /**
     * 就绪检查（比健康检查更严格，可扩展检查数据库连接等）
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "UP");
        return ResponseEntity.ok(result);
    }

    private String getUptime() {
        long uptimeMillis = System.currentTimeMillis() -
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
