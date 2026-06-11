package com.example.rikkeibank.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "Vui lòng thiết lập mã PIN cho tài khoản mới")
    @Size(min = 6, max = 6, message = "Mã PIN phải bao gồm đúng 6 chữ số")
    private String transactionPin;
}