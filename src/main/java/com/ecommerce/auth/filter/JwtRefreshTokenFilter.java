package com.ecommerce.auth.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.user.model.UserRole;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class JwtRefreshTokenFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        jwtProvider.validateRefreshToken(refreshToken);
        String subject = jwtProvider.getSubjectFromRefreshToken(refreshToken);
        String role = jwtProvider.getRoleFromRefreshToken(refreshToken);

        request.setAttribute("sub", subject);
        request.setAttribute("role", role);
        filterChain.doFilter(request, response);
    }
}
