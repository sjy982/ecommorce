package com.ecommerce.auth.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.ecommerce.auth.jwt.JwtProvider;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(new SecurityContextImpl()); // SecurityContext 초기화
    }

    @Test
    @DisplayName("유효한 Access Token으로 인증 정보를 설정하고 다음 필터로 진행해야 한다")
    void givenValidAccessToken_whenDoFilterInternal_thenSetAuthenticationAndProceed() throws Exception {
        // Given: 유효한 JWT 설정
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken"); // Authorization 헤더 추가

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class); // FilterChain Mocking

        // JwtProvider Mock 설정
        when(jwtProvider.resolveAccessToken(request)).thenReturn("validToken");
        when(jwtProvider.validateAccessToken("validToken")).thenReturn(true); // 토큰 검증 성공
        when(jwtProvider.getSubjectFromAccessToken("validToken")).thenReturn("user123");
        when(jwtProvider.getRoleFromToken("validToken")).thenReturn("USER");

        // When: 필터 실행
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: SecurityContextHolder에 인증 정보가 저장되었는지 확인
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user123", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                                        .getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));

        // 다음 필터가 호출되었는지 확인
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
