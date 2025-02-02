package com.ecommerce.auth.provider;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserInfoProvider {
    public OAuth2UserInfo getOAuth2UserInfo(String provider, OAuth2User oAuth2User) {
        if ("google".equals(provider)) {
            return new GoogleUserInfo(oAuth2User);
        }
        return null;
    }
}
