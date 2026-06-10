package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.model.dto.response.KycResponse;
import com.example.rikkeibank.security.CustomUserDetails;
import com.example.rikkeibank.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
    public class KycController {

    private final KycService kycService;

    // Yêu cầu Client gửi form-data, key là "file"
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycResponse>> uploadKyc(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // Lấy user đang đăng nhập từ JWT

        // Cản ngay nếu client không gửi file hoặc file rỗng
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng đính kèm file hình ảnh hợp lệ (CCCD/Passport).");
        }

        KycResponse kycResponse = kycService.uploadKyc(file, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.<KycResponse>builder()
                .success(true)
                .message("Tải lên hồ sơ eKYC thành công. Vui lòng chờ phê duyệt.")
                .data(kycResponse)
                .build());
    }
}