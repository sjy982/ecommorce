package com.ecommerce.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.user.Dto.RegisterUserRequestDto;
import com.ecommerce.user.Dto.RegisterUserResponseDto;
import com.ecommerce.user.Dto.TokenResponseDto;
import com.ecommerce.user.Exception.RefreshTokenException;
import com.ecommerce.user.Exception.SessionExpiredException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private RefreshTokenRedisService refreshTokenRedisService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRedisService userRedisService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유효한 세션 데이터로 사용자 등록")
    void givenValidSession_whenRegisterUser_thenShouldRegisterUserSuccessfully() {
        // Given
        String providerId = "provider123";
        String phone = "010-1234-5678";
        String address = "Test Address";
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto(phone, address);
        User user = new User();
        user.setProviderId(providerId);

        when(userRedisService.get(providerId)).thenReturn(user);
        when(jwtProvider.createAccessToken(providerId, UserRole.USER)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(providerId)).thenReturn("refresh-token");

        // When
        RegisterUserResponseDto response = userService.registerUser(providerId, requestDto);

        // Then
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(phone, response.getUser().getPhone());
        assertEquals(address, response.getUser().getAddress());
        assertNotEquals(null, response.getUser().getCart());
        verify(userRepository, times(1)).save(user);
        verify(userRedisService, times(1)).delete(providerId);
        verify(refreshTokenRedisService, times(1)).save(providerId, "refresh-token");
    }

    @Test
    @DisplayName("세션 데이터가 만료되어 사용자 등록 실패")
    void givenExpiredSession_whenRegisterUser_thenShouldThrowSessionExpiredException() {
        // Given
        String providerId = "provider123";
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto("010-1234-5678", "Test Address");

        when(userRedisService.get(providerId)).thenReturn(null);

        // When & Then
        assertThrows(SessionExpiredException.class, () -> userService.registerUser(providerId, requestDto));
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 새 토큰 발급")
    void givenValidRefreshToken_whenRefreshTokens_thenShouldIssueNewTokens() {
        // Given
        String providerId = "provider123";
        String refreshToken = "valid-refresh-token";

        when(refreshTokenRedisService.get(providerId)).thenReturn(refreshToken);
        when(jwtProvider.createAccessToken(providerId, UserRole.USER)).thenReturn("new-access-token");
        when(jwtProvider.createRefreshToken(providerId)).thenReturn("new-refresh-token");

        // When
        TokenResponseDto response = userService.refreshTokens(providerId, refreshToken);

        // Then
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(refreshTokenRedisService, times(1)).save(providerId, "new-refresh-token");
    }

    @Test
    @DisplayName("Refresh Token 불일치로 예외 발생")
    void givenMismatchedRefreshToken_whenRefreshTokens_thenShouldThrowRefreshTokenException() {
        // Given
        String providerId = "provider123";
        String refreshToken = "invalid-refresh-token";
        String storedRefreshToken = "valid-refresh-token";

        when(refreshTokenRedisService.get(providerId)).thenReturn(storedRefreshToken);

        // When & Then
        assertThrows(RefreshTokenException.class, () -> userService.refreshTokens(providerId, refreshToken));
        verify(refreshTokenRedisService, times(1)).delete(providerId);
    }

    @Test
    @DisplayName("저장된 Refresh Token이 없을 경우 예외 발생")
    void givenNoStoredRefreshToken_whenRefreshTokens_thenShouldThrowRefreshTokenException() {
        // Given
        String providerId = "provider123";
        String refreshToken = "invalid-refresh-token";

        when(refreshTokenRedisService.get(providerId)).thenReturn(null);

        // When & Then
        assertThrows(RefreshTokenException.class, () -> userService.refreshTokens(providerId, refreshToken));
    }
}
