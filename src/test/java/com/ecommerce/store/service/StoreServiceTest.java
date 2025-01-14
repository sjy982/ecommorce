package com.ecommerce.store.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.store.DTO.LoginStoreRequestDto;
import com.ecommerce.store.DTO.LoginStoreResponseDto;
import com.ecommerce.store.DTO.RegisterStoreRequestDto;
import com.ecommerce.store.DTO.RegisterStoreResponseDto;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;
import com.ecommerce.store.service.StoreService;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.service.RefreshTokenRedisService;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {
    @Mock
    private StoreRepository storeRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRedisService refreshTokenRedisService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StoreService storeService;
    @Test
    @DisplayName("Store 생성 - registerStore")
    void givenRegisterInfo_whenRegisterStore_thenShouldRegisterStoreSuccessful() {
        // Given
        RegisterStoreRequestDto requestDto = new RegisterStoreRequestDto();
        requestDto.setName("testName");
        requestDto.setPassword("testPw");
        requestDto.setPhoneNumber("010-1234-1234");

        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";

        String encryptedPassword = "encrypted password";

        when(storeRepository.existsByName(requestDto.getName())).thenReturn(false);
        when(jwtProvider.createAccessToken(requestDto.getName(), UserRole.STORE.name())).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(requestDto.getName(), UserRole.STORE.name())).thenReturn(refreshToken);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn(encryptedPassword);

        // When
        RegisterStoreResponseDto responseDto = storeService.registerStore(requestDto);

        // Then
        assertEquals(accessToken, responseDto.getAccessToken());
        assertEquals(refreshToken, responseDto.getRefreshToken());
        assertEquals("testName", responseDto.getStore().getName());
        assertEquals("010-1234-1234", responseDto.getStore().getPhoneNumber());
        assertEquals(encryptedPassword, responseDto.getStore().getPassword());
        assertEquals(0, responseDto.getStore().getTotalSales());

        verify(storeRepository, times(1)).save(responseDto.getStore());
        verify(refreshTokenRedisService, times(1)).save(responseDto.getStore().getName(), refreshToken);
    }

    @Test
    @DisplayName("올바른 아이디와 비번이 주어지면, 로그인에 성공해야 한다.")
    void givenCorrectIdAndPw_whenLoginStore_thenShouldLoginSuccessful() {
        // Given
        LoginStoreRequestDto requestDto = new LoginStoreRequestDto();
        requestDto.setName("testName");
        requestDto.setPassword("rawTestPw");

        Store store = Store.builder()
                           .name("testName")
                           .password("encrypted password")
                           .phoneNumber("010-1234-1234")
                           .build();

        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";

        when(storeRepository.findByName(requestDto.getName())).thenReturn(Optional.of(store));
        when(passwordEncoder.matches(requestDto.getPassword(), store.getPassword())).thenReturn(true);
        when(jwtProvider.createAccessToken(requestDto.getName(), UserRole.STORE.name())).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(requestDto.getName(), UserRole.STORE.name())).thenReturn(refreshToken);

        // When
        LoginStoreResponseDto responseDto = storeService.loginStore(requestDto);

        // Then
        assertEquals(accessToken, responseDto.getAccessToken());
        assertEquals(refreshToken, responseDto.getRefreshToken());
        verify(refreshTokenRedisService, times(1)).save(requestDto.getName(), refreshToken);
    }
}
