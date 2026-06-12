package com.example.rikkeibank.controller;

import com.example.rikkeibank.service.KycService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // IMPORT MỚI
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class KycControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KycService kycService;

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void uploadKyc_ShouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cccd_mat_truoc.jpg",
                "image/jpeg",
                "gia-lap-du-lieu-anh-binary".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/kyc/upload").file(file))
                .andExpect(status().isOk());
    }
}