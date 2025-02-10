package com.ecommerce.notification.dto;

import java.time.LocalDateTime;

import com.ecommerce.order.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private Long orderId;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
}
