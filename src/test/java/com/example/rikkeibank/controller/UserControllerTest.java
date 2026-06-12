package com.example.rikkeibank.controller;

import com.example.rikkeibank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // IMPORT MỚI THAY CHO MOCKBEAN
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Thay thế hoàn toàn @MockBean bằng @MockitoBean chuẩn bản 3.4+
    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllUsers_AsAdmin_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void getAllUsers_AsCustomer_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }
}