package com.ecommerce.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.ecommerce.user.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserRedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserRedisService userRedisService;
    private final Duration timeoutDuration = Duration.ofMinutes(10);
    private final String prefix = "signup:temp:";

    @Test
    @DisplayName("save 메서드가 정상적으로 Redis에 데이터를 저장해야 한다")
    void save_ShouldSaveUserInRedis() throws JsonProcessingException {
        // Given
        String key = "testKey";
        User user = User.builder()
                        .provider("test provider")
                        .providerId("test providerId")
                        .subject("test subject")
                        .email("test email")
                        .name("Test User")
                        .build();
        user.setName("Test User");
        String json = "{\"name\":\"Test User\"}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(user)).thenReturn(json);

        // When
        userRedisService.save(key, user);

        // Then
        verify(valueOperations, times(1)).set(eq(prefix + key), eq(json), eq(timeoutDuration));
    }

    @Test
    @DisplayName("get 메서드가 Redis에서 데이터를 가져와 User 객체로 변환해야 한다")
    void get_ShouldRetrieveUserFromRedis() throws JsonProcessingException {
        // Given
        String key = "testKey";
        String json = "{\"name\":\"Test User\"}";
        User user = new User();
        user.setName("Test User");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + key)).thenReturn(json);
        when(objectMapper.readValue(json, User.class)).thenReturn(user);

        // When
        User result = userRedisService.get(key);

        // Then
        assertNotNull(result);
        assertEquals("Test User", result.getName());
    }

    @Test
    @DisplayName("get 메서드가 Redis에서 null 데이터를 가져올 때 null을 반환해야 한다")
    void get_ShouldReturnNullWhenKeyDoesNotExist() {
        // Given
        String key = "nonexistentKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + key)).thenReturn(null);

        // When
        User result = userRedisService.get(key);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("get 메서드가 JSON 변환에 실패하면 RuntimeException을 던져야 한다")
    void get_ShouldThrowRuntimeException_WhenJsonDeserializationFails() throws JsonProcessingException {
        // Given
        String key = "testKey";
        String invalidJson = "invalid-json";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + key)).thenReturn(invalidJson);
        when(objectMapper.readValue(invalidJson, User.class)).thenThrow(new JsonProcessingException("Error") {});

        // When & Then
        assertThrows(RuntimeException.class, () -> userRedisService.get(key));
    }

    @Test
    @DisplayName("delete 메서드가 Redis에서 데이터를 삭제해야 한다")
    void delete_ShouldRemoveUserFromRedis() {
        // Given
        String key = "testKey";

        // When
        userRedisService.delete(key);

        // Then
        verify(redisTemplate, times(1)).delete(prefix + key);
    }
}
