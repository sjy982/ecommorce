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

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        int port = isRedisRunning() ? findAvailablePort() : redisPort;

        if (isArmMac()) {
            // ARM Mac용 바이너리를 사용
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

    private boolean isArmMac() {
        return Objects.equals(System.getProperty("os.arch"), "aarch64")
               && Objects.equals(System.getProperty("os.name"), "Mac OS X");
    }

    /**
     * ARM 아키텍처를 사용하는 Mac에서 실행할 수 있는 redis-server 바이너리 파일
     * test/resources/embedded-redis/redis-server-arm64
     */
    private File getRedisFileForArcMac() {
        try {
            // test/resources/embedded-redis/ 폴더 아래에 있는 바이너리
            return new ClassPathResource("embedded-redis/redis-server-arm64").getFile();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not load ARM Mac redis-server binary: " + e.getMessage(), e);
        }
    }

    private int findAvailablePort() {
        for (int port = 10000; port <= 65535; port++) {
            Process process = executeGrepProcessCommand(port);
            if (!isRunning(process)) {
                return port;
            }
        }
        throw new IllegalArgumentException("No available port found for embedded Redis.");
    }

    private boolean isRedisRunning() {
        return isRunning(executeGrepProcessCommand(redisPort));
    }

    private Process executeGrepProcessCommand(int redisPort) {
        String command = String.format("netstat -nat | grep LISTEN | grep %d", redisPort);
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
