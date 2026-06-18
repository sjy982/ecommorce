package com.ecommerce.payment.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ecommerce.payment.PaymentService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentFailedEventListener {
    private final PaymentService paymentService;

    @EventListener
    public void handle(PaymentFailedEvent event) {
        paymentService.markPaidFailed(event.paymentJobId());
    }
}
