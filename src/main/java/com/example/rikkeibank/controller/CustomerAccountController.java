package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.request.CreateAccountRequest;
import com.example.rikkeibank.model.dto.response.AccountBalanceResponse;
import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.security.CustomUserDetails;
import com.example.rikkeibank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
public class CustomerAccountController {

    private final AccountService accountService;

    // Xem danh sách toàn bộ tài khoản đang có
    @GetMapping("/my-accounts")
    public ResponseEntity<ApiResponse<List<AccountBalanceResponse>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AccountBalanceResponse> response = accountService.getAllMyAccounts(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.<List<AccountBalanceResponse>>builder()
                .success(true)
                .message("Lấy danh sách tài khoản thành công.")
                .data(response)
                .build());
    }

    // Mở thêm tài khoản mới
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> openNewAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAccountRequest request) {

        AccountBalanceResponse response = accountService.openNewAccount(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.<AccountBalanceResponse>builder()
                .success(true)
                .message("Mở tài khoản mới thành công.")
                .data(response)
                .build());
    }
}