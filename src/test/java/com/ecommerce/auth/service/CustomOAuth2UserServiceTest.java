package com.ecommerce.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ecommerce.auth.model.CustomOAuth2UserDetails;
import com.ecommerce.auth.provider.GoogleUserInfo;

import com.ecommerce.auth.provider.OAuth2UserInfoProvider;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {
    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;
    @Mock
    private UserRepository userInMemoryRepository;

    @Mock
    private OAuth2UserRequest userRequest;
    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private OAuth2UserInfoProvider oAuth2UserInfoProvider;

    @Mock
    private ClientRegistration clientRegistration;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("새로운 사용자가 로그인하면 TEMP 역할로 사용자 생성")
    void loadUserShouldCreateNewTempUserWhenUserDoesNotExist() {
        // Given
        String provider = "google";
        String subject = "12345";
        String email = "test@example.com";
        String name = "Test User";
        String providerId = provider + "_" + subject;
        when(oAuth2User.getAttributes()).thenReturn(Map.of("sub", subject, "email", email, "name", name));
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        when(clientRegistration.getRegistrationId()).thenReturn(provider);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(oAuth2UserInfoProvider.getOAuth2UserInfo(provider, oAuth2User))
                .thenReturn(new GoogleUserInfo(oAuth2User));
        when(userInMemoryRepository.findByLoginId(providerId)).thenReturn(null);

        // When
        CustomOAuth2UserDetails result = (CustomOAuth2UserDetails) customOAuth2UserService.loadUser(userRequest);

        // Then
        assertEquals(providerId, result.getUser().getProviderId());
        assertEquals(UserRole.TEMP, result.getUser().getRole());
        assertEquals(email, result.getUser().getEmail());
        assertEquals(name, result.getUser().getName());
        verify(userInMemoryRepository, times(1)).findByLoginId(providerId);
    }

    @Test
    @DisplayName("지원되지 않는 provider로 예외 발생")
    void loadUser_ShouldThrowException_WhenProviderIsUnsupported() {
        String unsupportedProvider = "unsupported";
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);
        when(clientRegistration.getRegistrationId()).thenReturn(unsupportedProvider);
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(oAuth2UserInfoProvider.getOAuth2UserInfo(unsupportedProvider, oAuth2User)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> customOAuth2UserService.loadUser(userRequest));
    }
}
