package com.ecommerce.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Authentication 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUser.username(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + customUser.role()))
        );

        context.setAuthentication(authentication);
        return context;
    }
}