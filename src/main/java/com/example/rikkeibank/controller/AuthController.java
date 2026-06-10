package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.request.ForgotPasswordRequest;
import com.example.rikkeibank.model.dto.request.LoginRequest;
import com.example.rikkeibank.model.dto.request.RefreshTokenRequest;
import com.example.rikkeibank.model.dto.request.RegisterRequest;
import com.example.rikkeibank.model.dto.request.ResetPasswordRequest;
import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.model.dto.response.TokenResponse;
import com.example.rikkeibank.security.CustomUserDetails;
import com.example.rikkeibank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.<TokenResponse>builder()
                .success(true)
                .message("Đăng nhập thành công")
                .data(tokenResponse)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.<TokenResponse>builder()
                .success(true)
                .message("Làm mới Token thành công")
                .data(tokenResponse)
                .build());
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String jwt = headerAuth.substring(7);
            authService.logout(jwt, userDetails.getId());
        }

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đăng xuất thành công")
                .build());
    }

    // ================== API QUÊN MẬT KHẨU ==================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Mã OTP đã được gửi đến email của bạn.")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.")
                .build());
    }
}