package com.ecommerce.payment.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException() {
        super("payment failed");
    }
}
