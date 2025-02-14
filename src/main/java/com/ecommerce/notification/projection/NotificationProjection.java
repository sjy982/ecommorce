package com.ecommerce.notification.projection;

import java.time.LocalDateTime;

import com.ecommerce.order.model.OrderStatus;

public interface NotificationProjection {
    Long getId();
    Long getOrderId();
    OrderStatus getOrderStatus();
    LocalDateTime getCreatedAt();
}
