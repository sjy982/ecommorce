package com.ecommerce.payment.job.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ecommerce.payment.job.model.PaymentJob;
import com.ecommerce.payment.job.model.PaymentJobStatus;
import com.ecommerce.payment.job.repository.PaymentJobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentJobService {
    private final PaymentJobRepository paymentJobRepository;

    public PaymentJob findById(Long id) {
        return paymentJobRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는다."));
    }

    public List<Long> pickNextBatch(int limit) {
        return paymentJobRepository.findBatchTargets(
                PaymentJobStatus.PENDING, PageRequest.of(0, limit));
    }


    @Transactional
    public void deletePaymentJob(PaymentJob job) {
        paymentJobRepository.delete(job);
    }

    @Transactional
    public void updateStatus(Long id, PaymentJobStatus status) {
        System.out.println("tx active = " + TransactionSynchronizationManager.isActualTransactionActive());
        int updated = paymentJobRepository.updateStatus(id, status);
        if(updated == 0) {
            throw new UsernameNotFoundException("존재x");
        }
    }
}
