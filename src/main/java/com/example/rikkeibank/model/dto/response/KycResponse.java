package com.example.rikkeibank.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KycResponse {
    private Long id;
    private String fullName;
    private String idNumber;
    private String status; // PENDING, CONFIRM, REJECT
    private String idCardFrontUrl;
    private LocalDateTime createdAt;
}