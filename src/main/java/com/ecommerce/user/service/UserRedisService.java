package com.ecommerce.user.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration timeoutDuration = Duration.ofMinutes(10);
    private final String prefix = "signup:temp:";
    @Autowired
    public UserRedisService(RedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(String key, User value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(prefix + key, json, timeoutDuration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing entity", e);
        }
    }

    public User get(String key) {
        String json = redisTemplate.opsForValue().get(prefix + key);
        if(json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing JSON to " + User.class.getName(), e);
        }
    }

    public void delete(String key) {
        redisTemplate.delete(prefix + key);
    }
}
