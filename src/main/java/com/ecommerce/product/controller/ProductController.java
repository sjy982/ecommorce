package com.ecommerce.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.product.dto.RegisterProductRequestDto;
import com.ecommerce.product.dto.RegisterProductResponseDto;
import com.ecommerce.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<RegisterProductResponseDto>> registerProduct(@RequestBody @Valid RegisterProductRequestDto dto) {
        String storeName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        RegisterProductResponseDto responseDto = productService.registerProduct(storeName, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseUtil.createResponse(HttpStatus.CREATED.value(), responseDto, "product created"));
    }
}
