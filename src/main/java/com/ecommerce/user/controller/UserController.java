package com.ecommerce.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.user.DTO.RegisterUserRequestDto;
import com.ecommerce.user.DTO.RegisterUserResponseDto;
import com.ecommerce.user.DTO.TokenResponseDto;
import com.ecommerce.user.model.Users;
import com.ecommerce.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping
    public ResponseEntity<ApiResponse<Users>> registerUser(@RequestBody @Valid RegisterUserRequestDto dto) {
        String providerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RegisterUserResponseDto registerUserResponseDto = userService.registerUser(providerId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Authorization", "Bearer " + registerUserResponseDto.getAccessToken())
                .header("Refresh-Token", registerUserResponseDto.getRefreshToken())
                .body(ApiResponseUtil.createResponse(HttpStatus.CREATED.value(), registerUserResponseDto.getUser(), "user created"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshAccessToken(@RequestHeader("Refresh-Token") String refreshToken,
                                                                @RequestAttribute("sub") String sub,
                                                                @RequestAttribute("role") String role) {
        TokenResponseDto tokens = userService.refreshTokens(sub, role, refreshToken);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .header("Refresh-Token", tokens.getRefreshToken())
                .body(ApiResponseUtil.createResponse(HttpStatus.CREATED.value(), "refresh tokens"));
    }
}
