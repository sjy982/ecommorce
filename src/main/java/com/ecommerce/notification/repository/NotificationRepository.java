package com.ecommerce.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.notification.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStoreIdAndIsReadFalseOrderByCreatedAtDesc(Long storeId);
    List<Notification> findByStoreIdOrderByCreatedAtDesc(Long storeId);
}
