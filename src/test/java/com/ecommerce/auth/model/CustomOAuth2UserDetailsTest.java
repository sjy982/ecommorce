package com.ecommerce.auth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import com.ecommerce.user.model.User;
import com.ecommerce.user.model.UserRole;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserDetailsTest {

    @Mock
    private User mockUser;

    @Test
    @DisplayName("사용자의 권한을 올바르게 반환해야 한다")
    void givenCustomOAuth2UserDetails_whenGetAuthorities_thenReturnCorrectUserRole() {
        // Given
        CustomOAuth2UserDetails userDetails = new CustomOAuth2UserDetails(mockUser, Map.of(), UserRole.USER.name());

        // When
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();

        // Then
        assertEquals("USER", authority.getAuthority());
    }

    @Test
    @DisplayName("사용자의 username을 올바르게 반환해야 한다")
    void givenCustomOAuth2UserDetails_whenGetUsername_thenReturnCorrectProviderId() {
        // Given
        when(mockUser.getProviderId()).thenReturn("provider123");
        CustomOAuth2UserDetails userDetails = new CustomOAuth2UserDetails(mockUser, Map.of(), UserRole.USER.name());

        // When
        String username = userDetails.getUsername();

        // Then
        assertEquals("provider123", username);
    }

    @Test
    @DisplayName("계정 상태가 항상 유효해야 한다")
    void givenCustomOAuth2UserDetails_whenCheckAccountState_thenReturnAlwaysValid() {
        // Given
        CustomOAuth2UserDetails userDetails = new CustomOAuth2UserDetails(mockUser, Map.of(), UserRole.USER.name());

        // Then
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }
}
