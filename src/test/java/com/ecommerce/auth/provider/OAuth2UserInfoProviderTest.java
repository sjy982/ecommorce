package com.ecommerce.auth.provider;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class OAuth2UserInfoProviderTest {

    private final OAuth2UserInfoProvider provider = new OAuth2UserInfoProvider();

    @Mock
    private OAuth2User mockOAuth2User;

    @Test
    @DisplayName("Google 프로바이더에 대해 GoogleUserInfo 반환")
    void givenGoogleProvider_whenGetOAuth2UserInfo_thenReturnGoogleUserInfo() {
        // Given
        String providerName = "google";

        // When
        OAuth2UserInfo result = provider.getOAuth2UserInfo(providerName, mockOAuth2User);

        // Then
        assertInstanceOf(GoogleUserInfo.class, result);
    }

    @Test
    @DisplayName("지원되지 않는 프로바이더에 대해 null 반환")
    void givenUnsupportedProvider_whenGetOAuth2UserInfo_thenReturnNull() {
        // Given
        String providerName = "unsupported";

        // When
        OAuth2UserInfo result = provider.getOAuth2UserInfo(providerName, mockOAuth2User);

        // Then
        assertNull(result);
    }
}
