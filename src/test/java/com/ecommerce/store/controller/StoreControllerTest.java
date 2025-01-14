package com.ecommerce.store.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.store.DTO.LoginStoreRequestDto;
import com.ecommerce.store.DTO.LoginStoreResponseDto;
import com.ecommerce.store.DTO.RegisterStoreRequestDto;
import com.ecommerce.store.DTO.RegisterStoreResponseDto;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    @Test
    @DisplayName("중복되지 않은 이름과 그외 가입 정보가 주어지면, 가입에 성공해야 하며 올바른 응답을 반환해야 한다.")
    void givenRegisterStoreRequestDto_whenRegisterStore_thenShouldReturnCorrectResponse() throws Exception {
        //Given
        RegisterStoreRequestDto requestDto = new RegisterStoreRequestDto();
        requestDto.setName("testName");
        requestDto.setPassword("testPw");
        requestDto.setPhoneNumber("010-1234-1234");

        String requestBody = objectMapper.writeValueAsString(requestDto);

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        RegisterStoreResponseDto responseDto = new RegisterStoreResponseDto(
                new Store(), accessToken, refreshToken);

        when(storeService.registerStore(requestDto)).thenReturn(responseDto);

        //When & Then
        mockMvc.perform(post("/api/store")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isCreated())
               .andExpect(header().exists("Authorization"))
               .andExpect(header().exists("Refresh-Token"));
    }

    @Test
    @DisplayName("올바른 아이디와 비번이 주어지면, 올바른 응답을 해야한다.")
    void givenLoginStoreRequestDto_whenLoginStore_thenShouldReturnLoginStoreResponseDto() throws Exception {
        // Given
        LoginStoreRequestDto requestDto = new LoginStoreRequestDto();
        requestDto.setName("testName");
        requestDto.setPassword("rawTestPw");

        String requestBody = objectMapper.writeValueAsString(requestDto);

        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";

        LoginStoreResponseDto responseDto = new LoginStoreResponseDto(accessToken, refreshToken);

        when(storeService.loginStore(requestDto)).thenReturn(responseDto);

        // When && Then
        mockMvc.perform(post("/api/store/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
               .andExpect(header().exists("Authorization"))
               .andExpect(header().exists("Refresh-Token"));
    }
}
