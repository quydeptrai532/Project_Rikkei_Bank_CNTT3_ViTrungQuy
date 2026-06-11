package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.UserUpdateRequest;
import com.example.rikkeibank.model.dto.response.UserResponseDto;
import com.example.rikkeibank.model.entity.Role;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        // Tận dụng luôn @Builder có sẵn của class Role
        mockRole = Role.builder()
                .id(1L)
                .name("ROLE_CUSTOMER")
                .description("Khách hàng")
                .build();

        mockUser = User.builder()
                .id(1L)
                .username("nguyenvana")
                .email("nva@gmail.com")
                .phoneNumber("0987654321")
                .isKyc(true)
                .role(mockRole)
                .build();
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponseDto response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("nguyenvana", response.getUsername());
        assertEquals("ROLE_CUSTOMER", response.getRoleName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateUser_ShouldReturnUpdatedDto_WhenValidRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("newemail@gmail.com");
        request.setPhoneNumber("0111222333");
        request.setIsActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponseDto response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("newemail@gmail.com", mockUser.getEmail());
        verify(userRepository, times(1)).save(mockUser);
    }
}