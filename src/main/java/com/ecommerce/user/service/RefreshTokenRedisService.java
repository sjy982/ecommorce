package com.ecommerce.user.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Duration timeoutDuration = Duration.ofDays(30);
    private final String prefix = "refreshToken:";

    @Autowired
    public RefreshTokenRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(prefix + key);
    }

    public void save(String key, String value) {
        redisTemplate.opsForValue().set(prefix + key, value, timeoutDuration);
    }

    public void delete(String key) {
        redisTemplate.delete(prefix + key);
    }
}
