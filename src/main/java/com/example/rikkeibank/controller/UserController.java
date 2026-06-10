package com.example.rikkeibank.controller;

import com.example.rikkeibank.model.dto.request.UserUpdateRequest;
import com.example.rikkeibank.model.dto.response.ApiResponse;
import com.example.rikkeibank.model.dto.response.UserResponseDto;
import com.example.rikkeibank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')") // Cho phép Admin và Staff thao tác theo đặc tả UC-02
public class UserController {

    private final UserService userService;

    // GET /api/v1/users?page=0&size=10
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserResponseDto> usersPage = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<UserResponseDto>>builder()
                .success(true)
                .message("Tải danh sách khách hàng thành công.")
                .data(usersPage)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable("id") Long id) {
        UserResponseDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Lấy thông tin chi tiết khách hàng thành công.")
                .data(userDto)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        UserResponseDto updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .success(true)
                .message("Cập nhật thông tin khách hàng thành công.")
                .data(updatedUser)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa khách hàng thành công.")
                .build());
    }
}