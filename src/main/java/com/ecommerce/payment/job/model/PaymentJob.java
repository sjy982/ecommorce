package com.ecommerce.payment.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PaymentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "VARCHAR(30) CHECK (status IN ('PENDING', 'PROCESSING', 'DONE', 'FAILED'))")
    private PaymentJobStatus status;

    public void markProcessing() {
        status = PaymentJobStatus.PROCESSING;
    }

    public void markDone() {
        status = PaymentJobStatus.DONE;
    }

    public void markFailed() {
        status = PaymentJobStatus.FAILED;
    }
}
