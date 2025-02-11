package com.ecommerce.notification.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.dto.NotificationResponseDto;
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

    public Notification createNotification(Store store, Orders order) {
        return notificationRepository.save(Notification.builder()
                                                   .store(store)
                                                   .order(order)
                                                   .build());
    }

    public List<NotificationResponseDto> getUnReadNotifications(Long storeId) {
        List<Notification> unReadNotifications = notificationRepository.findByStoreIdAndIsReadFalseOrderByCreatedAtDesc(storeId);
        return unReadNotifications.stream()
                            .map(notification -> NotificationResponseDto.builder()
                                                                        .id(notification.getId())
                                                                        .orderId(notification.getOrder().getId())
                                                                        .orderStatus(notification.getOrder().getStatus())
                                                                        .createdAt(notification.getCreatedAt()).build()).toList();
    }

    public List<NotificationResponseDto> getAllNotifications(Long storeId) {
        List<Notification> notifications = notificationRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        return notifications.stream()
                            .map(notification -> NotificationResponseDto.builder()
                                                                        .id(notification.getId())
                                                                        .orderId(notification.getOrder().getId())
                                                                        .orderStatus(notification.getOrder().getStatus())
                                                                        .createdAt(notification.getCreatedAt()).build()).toList();
    }

    public void markAsRead(Long notificationId) {
        int updatedCount = notificationRepository.markAsRead(notificationId);
        if(updatedCount == 0) {
            throw new UsernameNotFoundException("해당 알림을 찾을 수 없습니다.");
        }
    }
}
