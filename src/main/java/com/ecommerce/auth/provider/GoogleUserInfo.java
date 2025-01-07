package com.ecommerce.auth.provider;

import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo {
    private final OAuth2User oAuth2User;

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getSubject() {
        return (String) oAuth2User.getAttributes().get("sub");
    }

    @Override
    public String getEmail() {
        return (String) oAuth2User.getAttributes().get("email");
    }

    @Override
    public String getName() {
        return (String) oAuth2User.getAttributes().get("name");
    }
}
