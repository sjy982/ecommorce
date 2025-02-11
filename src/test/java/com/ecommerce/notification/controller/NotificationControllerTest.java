package com.ecommerce.notification.controller;

import static com.ecommerce.config.TestConstants.TEST_PROVIDER_ID;
import static com.ecommerce.config.TestConstants.TEST_STORE_ID;
import static com.ecommerce.config.TestConstants.TEST_STORE_ROLE;
import static com.ecommerce.config.TestConstants.TEST_USER_ROLE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.notification.dto.NotificationResponseDto;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Orders;
import com.ecommerce.security.WithMockCustomUser;
import com.ecommerce.store.model.Store;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser(username = TEST_STORE_ID, role = TEST_STORE_ROLE)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("api/notification/unread 요청하면 확인되지 않음 모든 알림을 응답으로 받는다.")
    void givenValidStoreUser_whenGetUnreadNotifications_thenShouldReturnIsReadFalseNotifications() throws Exception {
        // Given
        List<NotificationResponseDto> notifications = List.of(
                NotificationResponseDto.builder()
                                       .id(0L)
                                       .orderId(0L)
                                       .orderStatus(OrderStatus.PENDING)
                                       .createdAt(LocalDateTime.now()).build(),

                NotificationResponseDto.builder()
                                       .id(1L)
                                       .orderId(1L)
                                       .orderStatus(OrderStatus.PENDING)
                                       .createdAt(LocalDateTime.now()).build()
        );

        when(notificationService.getUnReadNotifications(1L)).thenReturn(notifications);

        // When && Then
        mockMvc.perform(get("/api/notification/unread")
                                .header("Authorization", "Bearer valid-access-token"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data[0].id").value(0))
               .andExpect(jsonPath("$.data[0].orderId").value(0))
               .andExpect(jsonPath("$.data[0].orderStatus").value("PENDING"))
               .andExpect(jsonPath("$.data[1].id").value(1))
               .andExpect(jsonPath("$.data[1].orderId").value(1))
               .andExpect(jsonPath("$.data[1].orderStatus").value("PENDING"));
    }

    @Test
    @DisplayName("api/notification 요청하면 모든 알림을 응답으로 받는다.")
    void givenValidStoreUser_whenGetAllNotifications_thenShouldReturnAllNotifications() throws Exception {
        // Given
        List<NotificationResponseDto> notifications = List.of(
                NotificationResponseDto.builder()
                                       .id(0L)
                                       .orderId(0L)
                                       .orderStatus(OrderStatus.PENDING)
                                       .createdAt(LocalDateTime.now()).build(),

                NotificationResponseDto.builder()
                                       .id(1L)
                                       .orderId(1L)
                                       .orderStatus(OrderStatus.PENDING)
                                       .createdAt(LocalDateTime.now()).build()
        );

        when(notificationService.getAllNotifications(1L)).thenReturn(notifications);

        // When && Then
        mockMvc.perform(get("/api/notification")
                                .header("Authorization", "Bearer valid-access-token"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data[0].id").value(0))
               .andExpect(jsonPath("$.data[0].orderId").value(0))
               .andExpect(jsonPath("$.data[0].orderStatus").value("PENDING"))
               .andExpect(jsonPath("$.data[1].id").value(1))
               .andExpect(jsonPath("$.data[1].orderId").value(1))
               .andExpect(jsonPath("$.data[1].orderStatus").value("PENDING"));
    }

    @Test
    @DisplayName("api/notification/{notificationId} 요청하면 notificationId의 알림은 읽음 처리가 된다.")
    void givenValidStoreUser_whenMarkNotificationAsRead_thenShouldIsReadTrue() throws Exception {
        mockMvc.perform(patch("/api/notification/1")
                                .header("Authorization", "Bearer valid-access-token"))
                .andExpect(status().isOk());
    }
}
