package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.request.KycApprovalRequest;
import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.model.dto.response.KycResponse;
import com.example.rikkeibank.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/kyc")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_ADMIN')") // Bảo vệ Endpoint theo ma trận phân quyền
public class StaffKycController {

    private final KycService kycService;

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<KycResponse>> approveKyc(
            @PathVariable("id") Long profileId,
            @Valid @RequestBody KycApprovalRequest request) {

        KycResponse response = kycService.approveKyc(profileId, request);

        return ResponseEntity.ok(ApiResponse.<KycResponse>builder()
                .success(true)
                .message("Xử lý phê duyệt hồ sơ eKYC thành công.")
                .data(response)
                .build());
    }
}