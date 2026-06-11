package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.RegisterRequest;
import com.example.rikkeibank.model.dto.request.ResetPasswordRequest;
import com.example.rikkeibank.model.entity.Role;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.RoleRepository;
import com.example.rikkeibank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Tên đăng nhập đã tồn tại!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_ShouldSuccess_WhenOtpIsCorrect() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newpass123");

        User mockUser = new User();
        mockUser.setEmail("test@gmail.com");

        // Mock hành vi của Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("OTP_test@gmail.com")).thenReturn("123456"); // Trả về đúng OTP

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newpass123")).thenReturn("encodedPass");

        assertDoesNotThrow(() -> authService.resetPassword(request));

        assertEquals("encodedPass", mockUser.getPassword());
        verify(userRepository, times(1)).save(mockUser);
        verify(redisTemplate, times(1)).delete("OTP_test@gmail.com"); // Đổi xong phải xóa OTP
    }
}