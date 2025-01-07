package com.ecommerce.auth.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ecommerce.auth.exception.TokenInvalidException;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;


@ExtendWith(MockitoExtension.class)
class JwtExceptionFilterTest {
    private ObjectMapper objectMapper;

    @Mock
    private FilterChain filterChain;

    private JwtExceptionFilter jwtExceptionFilter;
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        jwtExceptionFilter = new JwtExceptionFilter(objectMapper);
    }

    @Test
    @DisplayName("JwtException 발생 시 401 Unauthorized 응답 반환")
    void doFilterInternalWithJwtExceptionShouldReturnUnauthorizedResponse() throws Exception {
        // Given: Mock Request, Response, FilterChain
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // FilterChain에서 JwtException 발생하도록 설정
        doThrow(new TokenInvalidException("Invalid JWT Token"))
                .when(filterChain).doFilter(request, response);

        // When: 필터 실행
        jwtExceptionFilter.doFilterInternal(request, response, filterChain);

        // Then: 응답 상태 코드가 401인지 확인
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        ApiResponse<?> expectedResponse = ApiResponseUtil.createResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
        ApiResponse<?> actualResponse = objectMapper.readValue(response.getContentAsString(), ApiResponse.class);

        // timestamp외 나머지를 검증
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        assertEquals(expectedResponse.getData(), actualResponse.getData());
    }

    @Test
    @DisplayName("예외 없이 필터 체인을 정상적으로 통과")
    void doFilterInternalWithoutExceptionShouldProceedToNextFilter() throws Exception {
        // Given: Mock Request, Response, FilterChain
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // FilterChain이 예외를 던지지 않고 정상 실행되도록 설정
        doNothing().when(filterChain).doFilter(request, response);

        // When: 필터 실행
        jwtExceptionFilter.doFilterInternal(request, response, filterChain);

        // Then: 다음 필터가 호출되었는지 확인
        verify(filterChain, times(1)).doFilter(request, response);

        // 응답 상태 코드가 변경되지 않았는지 확인 (기본 상태는 200 OK)
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
