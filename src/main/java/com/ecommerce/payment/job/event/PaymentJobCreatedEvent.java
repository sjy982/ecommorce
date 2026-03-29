package com.ecommerce.payment.job.event;

import lombok.Builder;

@Builder
public class PaymentJobCreatedEvent {
    private final long id;
}
