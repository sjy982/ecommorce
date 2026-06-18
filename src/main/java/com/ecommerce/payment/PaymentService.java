package com.ecommerce.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.job.service.PaymentJobService;
import com.ecommerce.payment.job.model.PaymentJob;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final StoreService storeService;
    private final PaymentJobService paymentJobService;
    private final ProductService productService;

    @Transactional
    public void markPaid(Long paymentJobId) {
        //결제 완료 이후에 작업들.
        final PaymentJob paymentJob = paymentJobService.findById(paymentJobId);
        final Orders order = orderService.findByIdForUpdate(paymentJob.getOrderId());

        if (order.getStatus() == OrderStatus.PAID) {
            paymentJob.markDone();
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("pending payment order만 결제 완료 처리할 수 있습니다.");
        }

        long amount = order.getProduct().getPrice() * order.getQuantity();
        storeService.increaseTotalSales(order.getStore().getId(), amount); //원자적 연산

        order.markPaid();
        notificationService.createNotification(order);

        paymentJob.markDone();
    }

    @Transactional
    public void markPaidFailed(Long paymentJobId) {
        final PaymentJob paymentJob = paymentJobService.findById(paymentJobId);
        final Orders order = orderService.findByIdForUpdate(paymentJob.getOrderId());

        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            paymentJob.markFailed();
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("pending payment order만 결제 실패 처리할 수 있습니다.");
        }

        productService.increaseStock(order.getProduct().getId(), order.getQuantity());
        order.markFailed();
        paymentJob.markFailed();
    }
}
