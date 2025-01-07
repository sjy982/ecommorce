package com.ecommerce.auth.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ecommerce.auth.jwt.JwtProvider;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtRefreshTokenFilterTest {

    @Mock
    private JwtProvider jwtProvider; // Mock 객체

    @Mock
    private FilterChain filterChain; // Mock 객체

    @InjectMocks
    private JwtRefreshTokenFilter jwtRefreshTokenFilter;

    @Test
    @DisplayName("유효한 Refresh Token으로 필터 실행 시 'sub' 속성이 설정되고 필터 체인이 호출됨")
    void doFilterInternalWithValidRefreshTokenShouldSetSubjectAttributeAndProceed() throws Exception {
        // Given: Mock Request, Response
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Refresh-Token", "validRefreshToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // JwtProvider Mock 설정
        when(jwtProvider.resolveRefreshToken(request)).thenReturn("validRefreshToken");
        when(jwtProvider.validateRefreshToken("validRefreshToken")).thenReturn(true);
        when(jwtProvider.getSubjectFromRefreshToken("validRefreshToken")).thenReturn("user123");

        // When: 필터 실행
        jwtRefreshTokenFilter.doFilterInternal(request, response, filterChain);

        // Then: request에 "sub" 속성이 올바르게 설정되었는지 확인
        assertEquals("user123", request.getAttribute("sub"));

        // filterChain.doFilter()가 호출되었는지 확인
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
