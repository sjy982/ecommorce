package com.ecommerce.notification.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final StoreService storeService;
    private final OrderService orderService;

    public Notification createNotification(Long storeId, Long orderId) {
        Store store = storeService.findByIdStore(storeId);
        Orders order = orderService.findByIdOrder(orderId);
        return notificationRepository.save(Notification.builder()
                                                   .store(store)
                                                   .order(order)
                                                   .build());
    }

    public List<Notification> getUnReadNotifications(Long storeId) {
        return notificationRepository.findByStoreIdAndIsReadFalseOrderByCreatedAtDesc(storeId);
    }

    public List<Notification> getAllNotifications(Long storeId) {
        return notificationRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new UsernameNotFoundException("notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
