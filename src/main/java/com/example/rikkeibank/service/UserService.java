package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.UserUpdateRequest;
import com.example.rikkeibank.model.dto.response.UserResponseDto;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // UC-02: Lấy danh sách phân trang dùng JPQL Constructor Projection (Đã fix N+1 từ trước)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAllUsersWithProjection(pageable);
    }

    // FIX LỖI LAZY Ở ĐÂY: Thêm @Transactional(readOnly = true) để giữ Session DB mở
    // cho đến khi build xong DTO
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isKyc(user.getIsKyc())
                .roleName(user.getRole().getName()) // Lúc này Session vẫn còn, Hibernate sẽ tự select Role
                .build();
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(user);

        return UserResponseDto.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .phoneNumber(updatedUser.getPhoneNumber())
                .isKyc(updatedUser.getIsKyc())
                .roleName(updatedUser.getRole().getName())
                .build();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }
}