package com.ecommerce.auth.provider;

public interface OAuth2UserInfo {
    String getProvider();
    String getSubject();
    String getEmail();
    String getName();
}
