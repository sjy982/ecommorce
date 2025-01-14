package com.ecommerce.auth.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import com.ecommerce.auth.Dto.AuthenticationSuccessResponseDto;
import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.auth.model.CustomOAuth2UserDetails;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.service.RefreshTokenRedisService;
import com.ecommerce.user.service.UserRedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationSuccessHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRedisService userRedisService;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomOAuth2UserDetails userDetails;

    @Mock
    private RefreshTokenRedisService refreshTokenRedisService;

    private ObjectMapper objectMapper;

    private AuthenticationSuccessResponseDto successResponseDto;

    @InjectMocks
    private CustomAuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper(); // 실제 ObjectMapper 생성
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        successResponseDto = new AuthenticationSuccessResponseDto();

        // 실제 객체를 InjectMocks에 수동으로 주입
        successHandler = new CustomAuthenticationSuccessHandler(
                jwtProvider,
                objectMapper,
                userRedisService,
                refreshTokenRedisService,
                successResponseDto
        );
    }

    @Test
    @DisplayName("Temp User 로그인 성공 - Redis에 저장 및 Temp Token 반환")
    void givenTempUser_whenOnAuthenticationSuccess_thenSaveToRedisAndReturnTempToken() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        User tempUser = new User();
        tempUser.setProviderId("provider123");

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(tempUser);
        when(userDetails.getRole()).thenReturn(UserRole.TEMP.name());
        when(jwtProvider.createTempToken("provider123")).thenReturn("temp-token");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRedisService, times(1)).save("provider123", tempUser);
        assertEquals("Bearer temp-token", response.getHeader("Authorization"));

        ApiResponse<?> expectedResponse = ApiResponseUtil.createResponse(200, successResponseDto, "Please enter additional information");
        ApiResponse<?> actualResponse = objectMapper.readValue(
                response.getContentAsString(),
                new TypeReference<ApiResponse<AuthenticationSuccessResponseDto>>() {});

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        assertEquals(expectedResponse.getData(), actualResponse.getData());
    }

    @Test
    @DisplayName("일반 User 로그인 성공 - AccessToken과 RefreshToken 반환")
    void givenNormalUser_whenOnAuthenticationSuccess_thenReturnAccessAndRefreshTokens() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String providerId = "provider123";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        User normalUser = new User();
        normalUser.setProviderId(providerId);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(normalUser);
        when(userDetails.getRole()).thenReturn(UserRole.USER.name());
        when(jwtProvider.createAccessToken(providerId, UserRole.USER.name())).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(providerId, UserRole.USER.name())).thenReturn(refreshToken);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(refreshTokenRedisService, times(1)).save(providerId, refreshToken);
        assertEquals("Bearer access-token", response.getHeader("Authorization"));
        assertEquals("refresh-token", response.getHeader("Refresh-Token"));

        ApiResponse<?> expectedResponse = ApiResponseUtil.createResponse(200, "Login completed");
        ApiResponse<?> actualResponse = objectMapper.readValue(response.getContentAsString(), ApiResponse.class);

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        assertEquals(expectedResponse.getData(), actualResponse.getData());
    }
}
