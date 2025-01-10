package com.ecommerce.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ecommerce.auth.exception.TokenExpiredException;
import com.ecommerce.auth.exception.TokenInvalidException;
import com.ecommerce.auth.exception.TokenMissingException;
import com.ecommerce.user.model.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

class JwtProviderTest {
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                "Fml0SHk9d0fDh4kfDh6bEjg6a3dlVgh5TnNnIji4bH8=",
                "Abx7VkTjH8gH3YlqK6sJDh4qM1pV7bGhTjP0yLi8ZjQ=",
                2000L, // Access Token 유효 시간: 2초
                3000L, // Refresh Token 유효 시간: 3초
                2000L  // Temp Token 유효 시간: 2초
        );
    }

    @Test
    @DisplayName("Access Token 생성 - 유효한 토큰 반환")
    void givenUserAndRole_whenCreateAccessToken_thenReturnValidToken() {
        String token = jwtProvider.createAccessToken("user123", UserRole.USER);
        assertNotNull(token, "Access Token은 null이어서는 안 됩니다.");
    }

    @Test
    @DisplayName("Refresh Token 생성 - 유효한 토큰 반환")
    void givenUser_whenCreateRefreshToken_thenReturnValidToken() {
        String token = jwtProvider.createRefreshToken("user123");
        assertNotNull(token, "Refresh Token은 null이어서는 안 됩니다.");
    }

    @Test
    @DisplayName("Temp Token 생성 - 유효한 토큰 반환")
    void givenUser_whenCreateTempToken_thenReturnValidToken() {
        String token = jwtProvider.createTempToken("user123");
        assertNotNull(token, "Temp Token은 null이어서는 안 됩니다.");
    }

    @Test
    @DisplayName("Access Token 검증 - 유효한 토큰이면 true 반환")
    void givenValidAccessToken_whenValidateAccessToken_thenReturnTrue() {
        String token = jwtProvider.createAccessToken("user123", UserRole.USER);
        assertTrue(jwtProvider.validateAccessToken(token), "유효한 Access Token은 true를 반환해야 합니다.");
    }

    @Test
    @DisplayName("Access Token 검증 - 잘못된 서명 시 TokenInvalidException 발생")
    void givenInvalidSignedToken_whenValidateAccessToken_thenThrowTokenInvalidException() {
        String invalidSignedToken = Jwts.builder()
                                        .setSubject("user123")
                                        .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256)) // 다른 키로 서명
                                        .compact();
        assertThrows(TokenInvalidException.class, () -> jwtProvider.validateAccessToken(invalidSignedToken));
    }

    @Test
    @DisplayName("Access Token 검증 - 잘못된 형식 시 TokenInvalidException 발생")
    void givenMalformedToken_whenValidateAccessToken_thenThrowTokenInvalidException() {
        String malformedToken = "malformedTokenWithoutTwoDots";
        assertThrows(TokenInvalidException.class, () -> jwtProvider.validateAccessToken(malformedToken));
    }

    @Test
    @DisplayName("Access Token 검증 - 만료된 토큰 시 TokenExpiredException 발생")
    void givenExpiredAccessToken_whenValidateAccessToken_thenThrowTokenExpiredException() throws InterruptedException {
        String token = jwtProvider.createAccessToken("user123", UserRole.USER);
        TimeUnit.MILLISECONDS.sleep(2500); // 토큰 만료 대기 (2초 유효)
        assertThrows(TokenExpiredException.class, () -> jwtProvider.validateAccessToken(token));
    }

    @Test
    @DisplayName("Access Token에서 Subject 추출")
    void givenAccessToken_whenGetSubjectFromAccessToken_thenReturnCorrectSubject() {
        String token = jwtProvider.createAccessToken("user123", UserRole.USER);
        String subject = jwtProvider.getSubjectFromAccessToken(token);
        assertEquals("user123", subject, "추출된 subject는 원본과 일치해야 합니다.");
    }

    @Test
    @DisplayName("Access Token에서 Role 추출")
    void givenAccessToken_whenGetRoleFromToken_thenReturnCorrectRole() {
        String token = jwtProvider.createAccessToken("user123", UserRole.USER);
        String role = jwtProvider.getRoleFromToken(token);
        assertEquals("USER", role, "추출된 role은 원본과 일치해야 합니다.");
    }

    @Test
    @DisplayName("요청에서 Access Token 추출 - 유효한 헤더")
    void givenRequestWithAuthorizationHeader_whenResolveAccessToken_thenReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validAccessToken");

        String token = jwtProvider.resolveAccessToken(request);
        assertEquals("validAccessToken", token, "Authorization 헤더에서 추출한 토큰이 원본과 일치해야 합니다.");
    }

    @Test
    @DisplayName("요청에서 Access Token 추출 - 누락된 헤더 시 TokenMissingException 발생")
    void givenRequestWithoutAuthorizationHeader_whenResolveAccessToken_thenThrowTokenMissingException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(TokenMissingException.class, () -> jwtProvider.resolveAccessToken(request));
    }

    @Test
    @DisplayName("요청에서 Refresh Token 추출 - 유효한 헤더")
    void givenRequestWithRefreshTokenHeader_whenResolveRefreshToken_thenReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Refresh-Token", "validRefreshToken");

        String token = jwtProvider.resolveRefreshToken(request);
        assertEquals("validRefreshToken", token, "Refresh-Token 헤더에서 추출한 토큰이 원본과 일치해야 합니다.");
    }

    @Test
    @DisplayName("요청에서 Refresh Token 추출 - 누락된 헤더 시 TokenMissingException 발생")
    void givenRequestWithoutRefreshTokenHeader_whenResolveRefreshToken_thenThrowTokenMissingException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThrows(TokenMissingException.class, () -> jwtProvider.resolveRefreshToken(request));
    }
}
