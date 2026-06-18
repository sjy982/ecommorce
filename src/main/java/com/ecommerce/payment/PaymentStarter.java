package com.ecommerce.payment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.ecommerce.payment.event.PaymentFailedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentStarter {
    private final MockPaymentGateway paymentGateway;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void start(Long paymentJobId) {
        boolean paymentResult = paymentGateway.pay();
        if(paymentResult) {
            paymentService.markPaid(paymentJobId);
        } else {
            applicationEventPublisher.publishEvent(new PaymentFailedEvent(paymentJobId));
        }
    }
}
