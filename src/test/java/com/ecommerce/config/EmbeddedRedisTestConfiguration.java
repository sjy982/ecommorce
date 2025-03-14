package com.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@TestConfiguration
public class EmbeddedRedisTestConfiguration {

    // EmbeddedRedisConfig 인스턴스를 수동으로 생성
    private static final EmbeddedRedisConfig embeddedRedisConfig = new EmbeddedRedisConfig();

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        // Embedded Redis 서버를 시작하고 실제 포트를 얻은 후,
        embeddedRedisConfig.startRedis();
        // 해당 포트를 spring.data.redis.port 프로퍼티에 등록합니다.
        registry.add("spring.data.redis.port", () -> embeddedRedisConfig.getActualPort());
    }
}

