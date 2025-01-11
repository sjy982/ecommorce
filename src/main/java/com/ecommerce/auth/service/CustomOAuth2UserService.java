package com.ecommerce.auth.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.model.CustomOAuth2UserDetails;
import com.ecommerce.auth.provider.OAuth2UserInfo;
import com.ecommerce.auth.provider.OAuth2UserInfoProvider;
import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final OAuth2UserInfoProvider oAuth2UserInfoProvider;
    private final DefaultOAuth2UserService delegate;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = oAuth2UserInfoProvider.getOAuth2UserInfo(provider, oAuth2User);

        if (oAuth2UserInfo == null) {
            log.error("Unsupported provider or invalid user information: {}", provider);
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        String subject = oAuth2UserInfo.getSubject();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        String providerId = provider + "_" + subject;

        Optional<User> optionalUser = userRepository.findByProviderId(providerId);
        User user;
        UserRole role = UserRole.USER;
        if(optionalUser.isEmpty()) {
            user = User.builder()
                        .provider(provider)
                        .subject(subject)
                        .email(email)
                        .name(name)
                        .providerId(providerId)
                        .build();

            role = UserRole.TEMP;
        } else {
            user = optionalUser.get();
        }
        log.info(String.valueOf(user));

        return new CustomOAuth2UserDetails(user, oAuth2User.getAttributes(), role);
    }
}
