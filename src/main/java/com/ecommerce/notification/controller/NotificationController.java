package com.ecommerce.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.ApiResponseUtil;
import com.ecommerce.notification.dto.NotificationResponseDto;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/notification")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getUnreadNotifications() {
        Long storeId = Long.parseLong((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        List<NotificationResponseDto> responseDtoList = notificationService.getUnReadNotifications(storeId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseUtil.createResponse(HttpStatus.OK.value(), responseDtoList, "success"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getAllNotifications() {
        Long storeId = Long.parseLong((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        List<NotificationResponseDto> responseDtoList = notificationService.getAllNotifications(storeId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseUtil.createResponse(HttpStatus.OK.value(), responseDtoList, "success"));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseUtil.createResponse(HttpStatus.OK.value(), "알림이 확인됨"));
    }
}
