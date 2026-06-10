package com.example.rikkeibank.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionHistoryResponse {
    private String transactionCode;
    private String transactionType; // "CREDIT" hoặc "DEBIT"
    private BigDecimal amount;
    private String relatedAccountNumber; // Nếu mình chuyển thì hiện STK người nhận, nếu mình nhận thì hiện STK người chuyển
    private String description;
    private String status;
    private LocalDateTime createdAt;
}