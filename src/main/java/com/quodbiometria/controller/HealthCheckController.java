package com.quodbiometria.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @Value("${spring.application.name:quod-biometria}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> applicationInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("name", applicationName);
        info.put("version", applicationVersion);
        info.put("environment", activeProfile);

        Map<String, String> system = new HashMap<>();
        system.put("java", System.getProperty("java.version"));
        system.put("javaVendor", System.getProperty("java.vendor"));
        system.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));

        info.put("buildTime", getBuildTime());

        info.put("system", system);

        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("maxMemory", runtime.maxMemory());

        info.put("memory", memory);

        return ResponseEntity.ok(info);
    }

    private String getBuildTime() {
        try {
            Properties properties = new Properties();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("git.properties");
            if (resource.exists()) {
                properties.load(resource.getInputStream());
                return properties.getProperty("git.build.time");
            }
        } catch (IOException e) {
            // Silenciando exceção
        }

        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

}