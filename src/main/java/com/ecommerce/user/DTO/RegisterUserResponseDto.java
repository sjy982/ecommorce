package com.ecommerce.user.DTO;

import com.ecommerce.user.model.Users;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisterUserResponseDto {
    private Users user;
    private String accessToken;
    private String refreshToken;
}
