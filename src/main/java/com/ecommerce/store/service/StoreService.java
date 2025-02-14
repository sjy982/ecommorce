package com.ecommerce.store.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.sasl.AuthenticationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ecommerce.auth.jwt.JwtProvider;
import com.ecommerce.store.DTO.LoginStoreRequestDto;
import com.ecommerce.store.DTO.LoginStoreResponseDto;
import com.ecommerce.store.DTO.RegisterStoreRequestDto;
import com.ecommerce.store.DTO.RegisterStoreResponseDto;
import com.ecommerce.store.Exception.InvalidPasswordException;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;
import com.ecommerce.user.model.UserRole;
import com.ecommerce.user.service.RefreshTokenRedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterStoreResponseDto registerStore(RegisterStoreRequestDto dto) {
        if (storeRepository.existsByName(dto.getName())) {
            throw new DuplicateKeyException("Store name already exists: " + dto.getName());
        }

        Store newStore = Store.builder()
                              .name(dto.getName())
                              .password(passwordEncoder.encode(dto.getPassword()))
                              .phoneNumber(dto.getPhoneNumber())
                              .totalSales(0L)
                              .build();

        storeRepository.save(newStore);

        String accessToken = jwtProvider.createAccessToken(newStore.getName(), UserRole.STORE.name());
        String refreshToken = jwtProvider.createRefreshToken(newStore.getName(), UserRole.STORE.name());

        refreshTokenRedisService.save(newStore.getName(), refreshToken);

        return new RegisterStoreResponseDto(newStore, accessToken, refreshToken);
    }

    public LoginStoreResponseDto loginStore(LoginStoreRequestDto dto) {
        Optional<Store> optionalStore = storeRepository.findByName(dto.getName());
        if(optionalStore.isEmpty()) {
            throw new UsernameNotFoundException(dto.getName() + " Store not found");
        }
        String encodedPassword = optionalStore.get().getPassword();

        if(!passwordEncoder.matches(dto.getPassword(), encodedPassword)) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtProvider.createAccessToken(dto.getName(), UserRole.STORE.name());
        String refreshToken = jwtProvider.createRefreshToken(dto.getName(), UserRole.STORE.name());

        refreshTokenRedisService.save(dto.getName(), refreshToken);

        return new LoginStoreResponseDto(accessToken, refreshToken);
    }

    public Store findByIdStore(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new UsernameNotFoundException("store not found"));
        return store;
    }
}
