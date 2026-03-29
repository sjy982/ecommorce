package com.ecommerce.payment;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentStarter {
    private final MockPaymentGateway paymentGateway;
    private final PaymentService paymentService;

    public void start(Long paymentJobId) {
        boolean paymentResult = paymentGateway.pay();
        if(paymentResult) {
            paymentService.markPaid(paymentJobId);
        } else {
            paymentService.markPaidFailed(paymentJobId);
        }
    }
}
