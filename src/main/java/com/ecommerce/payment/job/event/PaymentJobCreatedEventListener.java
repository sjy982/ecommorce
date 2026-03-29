package com.ecommerce.payment.job.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ecommerce.payment.PaymentDispatcher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentJobCreatedEventListener {
    private final PaymentDispatcher paymentDispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentJobCreatedEvent event) {
        paymentDispatcher.tryStartDrain();
    }
}
