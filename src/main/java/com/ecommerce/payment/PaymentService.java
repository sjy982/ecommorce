package com.ecommerce.payment;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.event.PaymentFailedEvent;
import com.ecommerce.payment.job.model.PaymentJob;
import com.ecommerce.payment.job.service.PaymentJobService;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void markPaid(Long paymentJobId) {
        final PaymentJob paymentJob = paymentJobService.findById(paymentJobId);
        final Orders order = orderService.findById(paymentJob.getOrderId());
        long amount = order.getProduct().getPrice() * order.getQuantity();
        storeService.increaseTotalSales(order.getStore().getId(), amount);

        order.markPaid();
        notificationService.createNotification(order);

        paymentJob.markDone();
    }

    @Transactional
    public void markPaidFailed(Long paymentJobId) {
        final PaymentJob paymentJob = paymentJobService.findById(paymentJobId);
        final Orders order = orderService.findById(paymentJob.getOrderId());

        // 중복 처리 방어: 이미 실패 처리된 주문은 재고를 다시 복구하지 않는다
        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            return;
        }

        order.markFailed();
        productService.increaseStock(order.getProduct().getId(), order.getQuantity());
        paymentJob.markFailed();

        applicationEventPublisher.publishEvent(PaymentFailedEvent.builder()
                .orderId(order.getId()).build());
    }
}
