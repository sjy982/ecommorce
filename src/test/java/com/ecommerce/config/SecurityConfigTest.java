package com.ecommerce.config;

import static com.ecommerce.config.TestConstants.TEST_PROVIDER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.auth.exception.TokenInvalidException;
import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.user.Dto.RegisterUserRequestDto;
import com.ecommerce.user.controller.UserController;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserController userController;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("OAuth2 경로 접근 시 인증이 필요하다")
    void givenUnauthenticatedUser_whenAccessingOAuth2Endpoint_thenRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/login"))
               .andExpect(status().isFound());
    }

    @Test
    @DisplayName("Refresh Token 경로는 인증 없이 접근 가능하다. 물론 올바른 Refresh Token이 필요하다.")
    void givenValidRefreshToken_whenAccessingRefreshTokenEndpoint_thenAllowAccess() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";
        when(jwtProvider.resolveRefreshToken(any(HttpServletRequest.class))).thenReturn(refreshToken);
        when(jwtProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtProvider.getSubjectFromRefreshToken(refreshToken)).thenReturn(TEST_PROVIDER_ID);
        when(userController.refreshAccessToken(refreshToken, TEST_PROVIDER_ID))
                .thenReturn(ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(post("/api/users/refresh")
                                .header("Refresh-Token", refreshToken))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("올바르지 않은 Refresh Token이 주어졌을 때 401 Unauthorized가 반환된다.")
    void givenInvalidRefreshToken_whenAccessingRefreshTokenEndpoint_thenReturnUnauthorized() throws Exception {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";
        when(jwtProvider.resolveRefreshToken(any(HttpServletRequest.class))).thenReturn(invalidRefreshToken);
        when(jwtProvider.validateRefreshToken(invalidRefreshToken)).thenThrow(new TokenInvalidException("Token is invalid"));

        // When & Then
        mockMvc.perform(post("/api/users/refresh")
                                .header("Refresh-Token", invalidRefreshToken))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TEMP 권한이 없는 사용자는 /users 경로에 접근할 수 없다")
    void givenUserWithoutTempRole_whenAccessingUsersEndpoint_thenReturnForbidden() throws Exception {
        // Given
        String accessToken = "valid-access-token";

        when(jwtProvider.resolveAccessToken(any(HttpServletRequest.class))).thenReturn(accessToken);
        when(jwtProvider.validateAccessToken(accessToken)).thenReturn(true);
        when(jwtProvider.getSubjectFromAccessToken(accessToken)).thenReturn(TEST_PROVIDER_ID);
        when(jwtProvider.getRoleFromToken(accessToken)).thenReturn("USER");

        // When & Then
        mockMvc.perform(post("/api/users")
                                .header("Authorization", "Bearer " + accessToken))
               .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TEMP 권한이 있는 경우 /users에 접근할 수 있다")
    void givenUserWithTempRole_whenAccessingUsersEndpoint_thenAllowAccess() throws Exception {
        // Given
        String accessToken = "valid-access-token";
        String requestBody = "{\"phone\": \"010-1111-1111\", \"address\": \"test address\"}";

        when(jwtProvider.resolveAccessToken(any(HttpServletRequest.class))).thenReturn(accessToken);
        when(jwtProvider.validateAccessToken(accessToken)).thenReturn(true);
        when(jwtProvider.getSubjectFromAccessToken(accessToken)).thenReturn(TEST_PROVIDER_ID);
        when(jwtProvider.getRoleFromToken(accessToken)).thenReturn("TEMP");

        when(userController.registerUser(any(RegisterUserRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When & Then
        mockMvc.perform(post("/api/users")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType("application/json")
                                .content(requestBody))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("올바르지 않은 AccessToken이 주어졌을 때 401 Unauthorized를 반환한다.")
    void givenInvalidAccessToken_whenAccessingUsersEndpoint_thenReturnUnauthorized() throws Exception {
        // Given
        String invalidAccessToken = "invalid-access-token";
        when(jwtProvider.resolveAccessToken(any(HttpServletRequest.class))).thenReturn(invalidAccessToken);
        when(jwtProvider.validateAccessToken(invalidAccessToken)).thenThrow(new TokenInvalidException("Token is invalid"));

        // When & Then
        mockMvc.perform(post("/api/users/users")
                                .header("Authorization", "Bearer " + invalidAccessToken))
               .andExpect(status().isUnauthorized());
    }
}
