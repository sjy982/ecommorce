package com.ecommerce.auth.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecommerce.auth.Dto.AuthenticationSuccessResponseDto;
import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.auth.model.CustomOAuth2UserDetails;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.service.UserRedisService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtTokenProvider;
    private final ObjectMapper mapper;
    private final UserRedisService userRedisService;
    private final AuthenticationSuccessResponseDto authenticationSuccessResponseDto;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserDetails userDetails = (CustomOAuth2UserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        if(user.getRole().name().equals(UserRole.TEMP.name())) {
            String providerId = user.getProviderId();
            userRedisService.save(providerId, user);

            String tempToken = jwtTokenProvider.createTempToken(providerId);
            response.setHeader("Authorization", "Bearer " + tempToken);

            response.getWriter().write(mapper.writeValueAsString(
                    ApiResponseUtil.createResponse(HttpServletResponse.SC_OK, authenticationSuccessResponseDto, "Please enter additional information"))
            );
        } else {
            String accessToken = jwtTokenProvider.createAccessToken(user.getProviderId(), user.getRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getProviderId());
            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setHeader("Refresh-Token", refreshToken);
            response.getWriter().write(mapper.writeValueAsString(
                    ApiResponseUtil.createResponse(HttpServletResponse.SC_OK, "Login completed"))
            );
        }
    }
}
