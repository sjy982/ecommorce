package com.ecommerce.store.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginStoreResponseDto {
    private String accessToken;
    private String refreshToken;
}
