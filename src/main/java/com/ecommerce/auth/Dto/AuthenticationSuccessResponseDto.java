package com.ecommerce.auth.Dto;

import org.springframework.stereotype.Component;

import lombok.Data;
@Data
@Component
public class AuthenticationSuccessResponseDto {
    private final String requestFields = "[phone, address]";
    private final String requestUri = "/api/users";
    private final String requestMethod = "POST";
}
