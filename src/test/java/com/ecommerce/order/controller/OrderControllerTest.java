package com.ecommerce.order.controller;

import static com.ecommerce.config.TestConstants.TEST_PROVIDER_ID;
import static com.ecommerce.config.TestConstants.TEST_USER_ROLE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.order.DTO.OrderProductDto;
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.DTO.OrderProductResponseDto;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.security.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("유효한 사용자가 특정 상품을 주문했을 때 올바른 응답을 반환해야 한다.")
    @WithMockCustomUser(username = TEST_PROVIDER_ID, role = TEST_USER_ROLE)
    void givenValidUser_whenOrderProduct_thenShouldReturnCreatedResponse() throws Exception {
        // Given
        OrderProductRequestDto requestDto = OrderProductRequestDto.builder()
                .productId(1L)
                .phoneNumber("010-1234-1234")
                .deliveryAddress("test Address")
                .quantity(10).build();

        String requestBody = objectMapper.writeValueAsString(requestDto);

        OrderProductResponseDto responseDto = OrderProductResponseDto.builder()
                                                                     .orderProduct(OrderProductDto.builder()
                                                                                                  .name("test Name")
                                                                                                  .quantity(10).price(100L).
                                                                                                  build())
                                                                     .deliveryAddress("test Address")
                                                                     .phoneNumber("010-1234-1234").build();

        when(orderService.orderProduct(TEST_PROVIDER_ID, requestDto)).thenReturn(responseDto);

        // When && Then
        mockMvc.perform(post("/api/orders")
                                .header("Authorization", "Bearer valid-access-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderProduct.name").value("test Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-1234"))
                .andExpect(jsonPath("$.data.orderProduct.price").value(100))
                .andExpect(jsonPath("$.data.deliveryAddress").value("test Address"))
                .andExpect(jsonPath("$.data.orderProduct.quantity").value(10));
    }
}
