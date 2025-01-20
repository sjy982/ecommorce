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

import com.ecommerce.user.model.Users;
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
    @DisplayName("Redis에 데이터를 저장해야 한다")
    void givenKeyAndUser_whenSaveCalled_thenShouldSaveUserInRedis() throws JsonProcessingException {
        // Given
        String key = "testKey";
        Users user = Users.builder()
                          .provider("test provider")
                          .providerId("test providerId")
                          .subject("test subject")
                          .email("test email")
                          .name("Test User")
                          .build();
        String json = "{\"name\":\"Test User\"}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(user)).thenReturn(json);

        // When
        userRedisService.save(key, user);

        // Then
        verify(valueOperations, times(1)).set(eq(prefix + key), eq(json), eq(timeoutDuration));
    }

    @Test
    @DisplayName("Redis에서 데이터를 가져와 User 객체로 변환해야 한다")
    void givenKey_whenGetCalled_thenShouldRetrieveUserFromRedis() throws JsonProcessingException {
        // Given
        String key = "testKey";
        String json = "{\"name\":\"Test User\"}";
        Users user = new Users();
        user.setName("Test User");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + key)).thenReturn(json);
        when(objectMapper.readValue(json, Users.class)).thenReturn(user);

        // When
        Users result = userRedisService.get(key);

        // Then
        assertNotNull(result);
        assertEquals("Test User", result.getName());
    }

    @Test
    @DisplayName("Redis에서 키가 존재하지 않으면 null을 반환해야 한다")
    void givenNonexistentKey_whenGetCalled_thenShouldReturnNull() {
        // Given
        String key = "nonexistentKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + key)).thenReturn(null);

        // When
        Users result = userRedisService.get(key);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("JSON 변환에 실패하면 RuntimeException을 던져야 한다")
    void givenInvalidJson_whenGetCalled_thenShouldThrowRuntimeException() throws JsonProcessingException {
        // Given
        String key = "testKey";
        String invalidJson = "invalid-json";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + key)).thenReturn(invalidJson);
        when(objectMapper.readValue(invalidJson, Users.class)).thenThrow(new JsonProcessingException("Error") {});

        // When & Then
        assertThrows(RuntimeException.class, () -> userRedisService.get(key));
    }

    @Test
    @DisplayName("Redis에서 데이터를 삭제해야 한다")
    void givenKey_whenDeleteCalled_thenShouldRemoveUserFromRedis() {
        // Given
        String key = "testKey";

        // When
        userRedisService.delete(key);

        // Then
        verify(redisTemplate, times(1)).delete(prefix + key);
    }
}
