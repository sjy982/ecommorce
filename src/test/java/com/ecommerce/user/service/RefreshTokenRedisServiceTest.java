package com.ecommerce.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenRedisService refreshTokenRedisService;

    private final String prefix = "refreshToken:";
    private final String testKey = "user123";
    private final String testValue = "refresh-token-value";

    private final Duration timeoutDuration = Duration.ofDays(30);

    @Test
    @DisplayName("저장 - Redis에 값을 저장해야 한다")
    void save_ShouldStoreValueInRedis() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        refreshTokenRedisService.save(testKey, testValue);

        // Then
        verify(valueOperations, times(1))
                .set(eq(prefix + testKey), eq(testValue), eq(timeoutDuration));
    }

    @Test
    @DisplayName("조회 - Redis에서 저장된 값을 반환해야 한다")
    void get_ShouldReturnValueFromRedis() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(prefix + testKey)).thenReturn(testValue);

        // When
        String result = refreshTokenRedisService.get(testKey);

        // Then
        assertEquals(testValue, result);
        verify(valueOperations, times(1)).get(prefix + testKey);
    }

    @Test
    @DisplayName("삭제 - Redis에서 값을 삭제해야 한다")
    void delete_ShouldRemoveValueFromRedis() {
        // When
        refreshTokenRedisService.delete(testKey);

        // Then
        verify(redisTemplate, times(1)).delete(prefix + testKey);
    }
}
