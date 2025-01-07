package com.ecommerce.common;

import static com.ecommerce.config.TestConstants.TEST_PROVIDER_ID;
import static com.ecommerce.config.TestConstants.TEST_TEMP_ROLE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.security.WithMockCustomUser;
import com.ecommerce.user.Dto.RegisterUserRequestDto;
import com.ecommerce.user.Exception.RefreshTokenException;
import com.ecommerce.user.Exception.SessionExpiredException;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("유효하지 않은 DTO 요청으로 BAD_REQUEST 응답을 반환해야 한다")
    void handleValidationExceptions_ShouldReturnBadRequest() throws Exception {
        // Given: 잘못된 DTO 요청
        String invalidRequestBody = "{}";

        // When & Then
        mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequestBody))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Validation failed. Please correct the highlighted fields."));
    }

    @Test
    @DisplayName("SessionExpiredException이 발생하면 UNAUTHORIZED 응답을 반환해야 한다")
    @WithMockCustomUser(username = TEST_PROVIDER_ID, role = TEST_TEMP_ROLE)
    void handleSessionExpiredException_ShouldReturnUnauthorized() throws Exception {
        // Given: Service에서 예외 발생 설정
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
        requestDto.setPhone("010-1234-4567");
        requestDto.setAddress("Test Address");
        when(userService.registerUser(TEST_PROVIDER_ID, requestDto)).thenThrow(new SessionExpiredException("Session expired"));

        String requestBody = objectMapper.writeValueAsString(requestDto);

        // When & Then: 요청 본문과 함께 테스트 실행
        mockMvc.perform(post("/api/users")
                                .header("Authorization", "Bearer temp-access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("session has expired."));
    }


    @Test
    @DisplayName("RefreshTokenException이 발생하면 UNAUTHORIZED 응답을 반환해야 한다")
    void handleRefreshTokenException_ShouldReturnUnauthorized() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";
        String sub = "test-providerId";

        when(userService.refreshTokens(sub, refreshToken)).thenThrow(new RefreshTokenException("Refresh Token mismatch or expired"));

        // When & Then
        mockMvc.perform(post("/api/users/refresh")
                                .header("Refresh-Token", refreshToken)
                                .requestAttr("sub", sub))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("Refresh Token mismatch or expired"));
    }

    @Test
    @DisplayName("JsonProcessingException이 발생하면 INTERNAL_SERVER_ERROR 응답을 반환해야 한다")
    @WithMockCustomUser(username = TEST_PROVIDER_ID, role = TEST_TEMP_ROLE)
    void handleJsonProcessingException_ShouldReturnInternalServerError() throws Exception {
        // Given: Service에서 예외 발생 설정
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
        requestDto.setPhone("010-1234-4567");
        requestDto.setAddress("Test Address");

        when(userService.registerUser(TEST_PROVIDER_ID, requestDto)).thenThrow(new RuntimeException("Error deserializing JSON to ..."));

        String requestBody = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/api/users")
                                .header("Authorization", "Bearer temp-access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.message").value("Internal Servel Error"));
    }
}
