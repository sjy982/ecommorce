package com.ecommerce.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStoreIdAndIsReadFalseOrderByCreatedAtDesc(Long storeId);
    List<Notification> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId")
    int markAsRead(@Param("notificationId") Long notificationId);
}
