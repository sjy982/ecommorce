package com.ecommerce.store.DTO;

import com.ecommerce.store.model.Store;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisterStoreResponseDto {
    private Store store;
    private String accessToken;
    private String refreshToken;
}
