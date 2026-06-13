package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.request.TransferRequest;
import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.model.dto.response.TransactionHistoryResponse;
import com.example.rikkeibank.model.entity.Transaction;
import com.example.rikkeibank.security.CustomUserDetails;
import com.example.rikkeibank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
public class CustomerTransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<String>> transferMoney(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {

        Transaction tx = transactionService.performTransfer(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Giao dịch chuyển khoản thực hiện thành công.")
                .data("Mã giao dịch: " + tx.getTransactionCode())
                .build());
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TransactionHistoryResponse>>> getTransactionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String accountNumber, // YÊU CẦU TRUYỀN SỐ TÀI KHOẢN TỪ URL
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionHistoryResponse> historyPage = transactionService.getTransactionHistory(userDetails.getId(), accountNumber, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<TransactionHistoryResponse>>builder()
                .success(true)
                .message("Tải sao kê lịch sử giao dịch thành công.")
                .data(historyPage)
                .build());
    }
}