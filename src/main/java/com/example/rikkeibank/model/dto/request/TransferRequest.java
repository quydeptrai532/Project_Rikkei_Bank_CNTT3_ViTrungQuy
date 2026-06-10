package com.example.rikkeibank.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "Số tài khoản nguồn không được để trống")
    private String fromAccountNumber;

    @NotBlank(message = "Số tài khoản đích không được để trống")
    private String targetAccountNumber;

    @NotNull(message = "Số tiền chuyển không được để trống")
    @DecimalMin(value = "1000.0", message = "Số tiền chuyển tối thiểu là 1000 VNĐ")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "Mã PIN giao dịch không được để trống")
    private String transactionPin;
}