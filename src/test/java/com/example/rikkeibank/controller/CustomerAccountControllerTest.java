package com.example.rikkeibank.controller;

import com.example.rikkeibank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // IMPORT MỚI
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void getMyAccounts_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/customer/accounts/my-accounts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void openAccount_WithInvalidData_ShouldReturn400() throws Exception {
        String invalidJson = "{\"transactionPin\": \"123\"}"; // Quy định PIN bắt buộc phải đủ 6 ký tự số
        mockMvc.perform(post("/api/v1/customer/accounts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}