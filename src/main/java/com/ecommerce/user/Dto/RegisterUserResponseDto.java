package com.ecommerce.user.Dto;

import com.ecommerce.user.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RegisterUserResponseDto {
    private User user;
    private String accessToken;
    private String refreshToken;
}
