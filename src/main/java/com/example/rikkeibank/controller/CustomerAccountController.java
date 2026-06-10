package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.response.AccountBalanceResponse;
import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.security.CustomUserDetails;
import com.example.rikkeibank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')") // Kiểm soát truy cập chặt chẽ Role-Based cho Customer
public class CustomerAccountController {

    private final AccountService accountService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> getAccountBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AccountBalanceResponse response = accountService.getBalance(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.<AccountBalanceResponse>builder()
                .success(true)
                .message("Vấn tin số dư tài khoản thành công.")
                .data(response)
                .build());
    }
}