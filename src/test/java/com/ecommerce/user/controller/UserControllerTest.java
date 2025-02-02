package com.ecommerce.user.controller;

import static com.ecommerce.config.TestConstants.TEST_PROVIDER_ID;
import static com.ecommerce.config.TestConstants.TEST_TEMP_ROLE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.security.WithMockCustomUser;
import com.ecommerce.user.DTO.RegisterUserRequestDto;
import com.ecommerce.user.DTO.RegisterUserResponseDto;
import com.ecommerce.user.DTO.TokenResponseDto;
import com.ecommerce.user.model.Users;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("새로운 사용자를 등록하고 올바른 응답을 반환해야 한다")
    @WithMockCustomUser(username = TEST_PROVIDER_ID, role = TEST_TEMP_ROLE)
    void givenRegisterUserRequest_whenValidRequest_thenShouldReturnCreatedResponse() throws Exception {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
        requestDto.setPhone("010-1234-5689");
        requestDto.setAddress("Test Address");

        String requestBody = objectMapper.writeValueAsString(requestDto);

        RegisterUserResponseDto responseDto = new RegisterUserResponseDto(
                new Users(), "access-token", "refresh-token");

        // Mock 설정
        when(userService.registerUser(TEST_PROVIDER_ID, requestDto)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/users")
                                .header("Authorization", "Bearer temp-access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
               .andExpect(status().isCreated())
               .andExpect(header().exists("Authorization"))
               .andExpect(header().exists("Refresh-Token"))
               .andExpect(jsonPath("$.message").value("user created"));
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 새로운 Access Token과 Refresh Token을 반환해야 한다")
    void givenValidRefreshToken_whenRefreshingTokens_thenShouldReturnNewTokens() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";
        String sub = "test-providerId";

        TokenResponseDto tokenResponse = new TokenResponseDto("new-access-token", "new-refresh-token");

        // Mock 설정
        when(userService.refreshTokens(sub, UserRole.USER.name(), refreshToken)).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/users/refresh")
                                .header("Refresh-Token", refreshToken)
                                .requestAttr("sub", sub)
                                .requestAttr("role", UserRole.USER.name()))
               .andExpect(status().isCreated())
               .andExpect(header().string("Authorization", "Bearer new-access-token"))
               .andExpect(header().string("Refresh-Token", "new-refresh-token"))
               .andExpect(jsonPath("$.message").value("refresh tokens"));
    }
}
