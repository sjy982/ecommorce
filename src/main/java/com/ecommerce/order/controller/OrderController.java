package com.ecommerce.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.DTO.OrderProductResponseDto;
import com.ecommerce.order.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderProductResponseDto>> orderProduct(@RequestBody @Valid OrderProductRequestDto dto) {
        String providerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        OrderProductResponseDto responseDto = orderService.orderProduct(providerId, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseUtil.createResponse(HttpStatus.OK.value(), responseDto, "order success"));
    }

//    @GetMapping("/user/{orderId}")
//    public ResponseEntity<ApiResponse<OrderProductResponseDto>> getOrder() {
//        String providerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
//
//    @GetMapping("/store/{orderId}")
//    public ResponseEntity<ApiResponse<OrderProductResponseDto>> getOrder() {
//        String providerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
}
