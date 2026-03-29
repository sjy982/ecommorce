package com.ecommerce.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.job.service.PaymentJobService;
import com.ecommerce.payment.job.model.PaymentJob;
import com.ecommerce.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final StoreService storeService;
    private final PaymentJobService paymentJobService;

    @Transactional
    public void markPaid(Long paymentJobId) {
        //결제 완료 이후에 작업들.
        final PaymentJob paymentJob = paymentJobService.findById(paymentJobId);
        final Orders order = orderService.findById(paymentJob.getOrderId());
        long amount = order.getProduct().getPrice() * order.getQuantity();
        storeService.increaseTotalSales(order.getStore().getId(), amount); //원자적 연산

        order.markPaid();
        notificationService.createNotification(order);

        paymentJobService.deletePaymentJob(paymentJob);
    }

    public void markPaidFailed(Long paymentJobId) {
        //결제 실패 이후에 작업들
    }
}
