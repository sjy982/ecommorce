package com.ecommerce.payment.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentFailedEventListener {
    private final NotificationService notificationService;
    private final OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentFailedEvent event) {
        Orders order = orderService.findById(event.getOrderId());
        notificationService.createNotification(order);
    }
}
