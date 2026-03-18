package com.ecommerce.payment;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class MockPaymentGateway {
    public boolean pay() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException("모의 결제 중 인터럽트 발생", e);
        }

//        boolean success = ThreadLocalRandom.current().nextInt(100) < 70;
        return true;
    }
}
