package com.ecommerce.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.store.DTO.LoginStoreRequestDto;
import com.ecommerce.store.DTO.LoginStoreResponseDto;
import com.ecommerce.store.DTO.RegisterStoreRequestDto;
import com.ecommerce.store.DTO.RegisterStoreResponseDto;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.service.StoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    @PostMapping
    public ResponseEntity<ApiResponse<Store>> registerStore(@RequestBody @Valid RegisterStoreRequestDto dto) {
        RegisterStoreResponseDto registerStoreResponseDto = storeService.registerStore(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Authorization", registerStoreResponseDto.getAccessToken())
                .header("Refresh-Token", registerStoreResponseDto.getRefreshToken())
                .body(ApiResponseUtil.createResponse(HttpStatus.CREATED.value(), registerStoreResponseDto.getStore(), "store created"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> loginStore(@RequestBody @Valid LoginStoreRequestDto dto) {
        LoginStoreResponseDto loginStoreResponseDto = storeService.loginStore(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Authorization", "Bearer " + loginStoreResponseDto.getAccessToken())
                .header("Refresh-Token", loginStoreResponseDto.getRefreshToken())
                .body(ApiResponseUtil.createResponse(HttpStatus.OK.value(), "store login success"));
    }
}
