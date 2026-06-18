package com.ecommerce.payment.event;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentFailedEvent {
    private final long orderId;
}
