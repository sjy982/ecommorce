package com.ecommerce.notification.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.dto.NotificationResponseDto;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.projection.NotificationProjection;
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

    public Notification createNotification(Orders order) {
        return notificationRepository.save(Notification.builder()
                                                   .order(order)
                                                   .build());
    }

    public List<NotificationProjection> getUnReadNotifications(Long storeId) {
        List<NotificationProjection> unReadNotifications = notificationRepository.findUnreadNotificationsByStoreId(storeId);
        return unReadNotifications;
    }

    public List<NotificationProjection> getAllNotifications(Long storeId) {
        List<NotificationProjection> notifications = notificationRepository.findNotificationsByStoreId(storeId);
        return notifications;
    }

    public void markAsRead(Long notificationId, Long storeId) {
        int updatedCount = notificationRepository.markAsRead(notificationId, storeId);
        if(updatedCount == 0) {
            throw new UsernameNotFoundException("해당 알림을 찾을 수 없습니다.");
        }
    }
}
