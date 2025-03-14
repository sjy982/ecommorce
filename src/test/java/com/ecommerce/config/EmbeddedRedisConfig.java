package com.ecommerce.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

@Slf4j
@Profile("test")
@Configuration
public class EmbeddedRedisConfig {

    private static final String REDIS_SERVER_MAX_MEMORY = "maxmemory 128M";

    // 기본값 6379를 지정할 수도 있음 (application-test.properties에 정의해도 됨)
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    // 실제 사용한 포트를 저장할 필드
    private int actualPort;

    @PostConstruct
    public void startRedis() {
        // 기본 포트가 이미 사용 중이면 사용 가능한 다른 포트를 찾음
        int port = isRedisRunning() ? findAvailablePort() : redisPort;
        actualPort = port; // 실제 사용한 포트를 저장

        if (isArmMac()) {
            // ARM Mac용 바이너리 사용
            redisServer = new RedisServer(getRedisFileForArcMac(), port);
        } else {
            // 일반 x86 등
            redisServer = RedisServer.builder()
                                     .port(port)
                                     .setting(REDIS_SERVER_MAX_MEMORY)
                                     .build();
        }

        try {
            redisServer.start();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to start embedded Redis: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    // 실제 사용 포트를 반환하는 getter
    public int getActualPort() {
        return actualPort;
    }

    private boolean isArmMac() {
        return Objects.equals(System.getProperty("os.arch"), "aarch64")
               && Objects.equals(System.getProperty("os.name"), "Mac OS X");
    }

    private File getRedisFileForArcMac() {
        try {
            return new ClassPathResource("embedded-redis/redis-server-arm64").getFile();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load ARM Mac redis-server binary: " + e.getMessage(), e);
        }
    }

    private int findAvailablePort() {
        for (int port = 10000; port <= 65535; port++) {
            if (!isPortInUse(port)) {
                return port;
            }
        }
        throw new IllegalArgumentException("No available port found for embedded Redis.");
    }

    private boolean isRedisRunning() {
        return isPortInUse(redisPort);
    }

    private boolean isPortInUse(int port) {
        Process process = executeGrepProcessCommand(port);
        return isRunning(process);
    }

    private Process executeGrepProcessCommand(int port) {
        String command = String.format("netstat -nat | grep LISTEN | grep %d", port);
        String[] shell = {"/bin/sh", "-c", command};
        try {
            return Runtime.getRuntime().exec(shell);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error executing netstat command: " + e.getMessage(), e);
        }
    }

    private boolean isRunning(Process process) {
        StringBuilder pidInfo = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading process output: " + e.getMessage(), e);
        }
        return StringUtils.hasText(pidInfo.toString());
    }
}
