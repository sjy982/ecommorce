package com.ecommerce.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.projection.NotificationProjection;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n.id AS id, o.id AS orderId, o.status AS orderStatus, n.createdAt AS createdAt " +
           "FROM Notification n JOIN n.order o " +
           "WHERE o.store.id = :storeId AND n.isRead = false " +
           "ORDER BY n.createdAt DESC")
    List<NotificationProjection> findUnreadNotificationsByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT n.id AS id, o.id AS orderId, o.status AS orderStatus, n.createdAt AS createdAt " +
           "FROM Notification n JOIN n.order o " +
           "WHERE o.store.id = :storeId " +
           "ORDER BY n.createdAt DESC")
    List<NotificationProjection> findNotificationsByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
           "WHERE n.id = :notificationId AND n.order.store.id = :storeId")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("storeId") Long storeId);
}
