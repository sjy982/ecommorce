package com.ecommerce.payment;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.ecommerce.payment.job.model.PaymentJobStatus;
import com.ecommerce.payment.job.service.PaymentJobService;

@Component
public class PaymentDispatcher {
    private static final Logger log = LoggerFactory.getLogger(PaymentDispatcher.class);

    private final PaymentJobService paymentJobService;
    private final PaymentStarter paymentStarter;
    private final ThreadPoolTaskExecutor paymentExecutor;
    private final ThreadPoolTaskExecutor paymentDispatcherExecutor;

    private final AtomicBoolean draining = new AtomicBoolean(false);
    private final AtomicBoolean newWorkRequested = new AtomicBoolean(false);
    private final AtomicBoolean wakeOnCompletion = new AtomicBoolean(false);
    private final Semaphore permits = new Semaphore(90);

    public PaymentDispatcher(PaymentJobService paymentJobService, PaymentStarter paymentStarter,
                             @Qualifier("paymentExecutor") ThreadPoolTaskExecutor paymentExecutor,
                             @Qualifier("paymentDispatcherExecutor") ThreadPoolTaskExecutor paymentDispatcherExecutor) {
        this.paymentJobService = paymentJobService;
        this.paymentStarter = paymentStarter;
        this.paymentExecutor = paymentExecutor;
        this.paymentDispatcherExecutor = paymentDispatcherExecutor;
    }

    public void tryStartDrain() {
        if (!draining.compareAndSet(false, true)) {
            newWorkRequested.set(true);
            return;
        }

        paymentDispatcherExecutor.execute(() -> {
            try {
                drain();
            } finally {
                draining.set(false);
                final boolean shouldRunAgain = newWorkRequested.getAndSet(false);
                if (shouldRunAgain) {
                    tryStartDrain();
                }
            }
        });
    }
    private void drain() {
        while (true) {
            if (permits.availablePermits() == 0) {
                permits.acquireUninterruptibly();
                permits.release();
            }

            final int capacity = Math.min(10, permits.availablePermits());
            List<Long> jobIds = paymentJobService.pickNextBatch(capacity);
            if (jobIds.isEmpty()) {
                return;
            }

            for (Long jobId : jobIds) {
                try {
                    permits.acquireUninterruptibly();
                    paymentJobService.updateStatus(jobId, PaymentJobStatus.PROCESSING);
                    paymentExecutor.execute(() -> {
                        try {
                            paymentStarter.start(jobId);
                        } finally {
                            permits.release();
                        }
                    });
                } catch (TaskRejectedException ex) {
                    permits.release();
                    paymentJobService.updateStatus(jobId, PaymentJobStatus.PENDING);

                    if (paymentExecutor.getThreadPoolExecutor().isShutdown()) {
                        log.error("PaymentExecutor가 shutdown됨.");
                        return;
                    }
                    log.error("PaymentExecutor가 Reject됨 설계상 나오면 안됨.");
                    return;
                } catch (RuntimeException ex) {
                    permits.release();
                    paymentJobService.updateStatus(jobId, PaymentJobStatus.PENDING);
                    log.error(ex.getMessage());
                    return;
                }
            }
        }
    }
}
