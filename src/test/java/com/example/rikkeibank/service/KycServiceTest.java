package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.KycApprovalRequest;
import com.example.rikkeibank.model.dto.response.KycResponse;
import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.model.entity.KycProfile;
import com.example.rikkeibank.model.entity.Status;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.AccountRepository;
import com.example.rikkeibank.repository.KycProfileRepository;
import com.example.rikkeibank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock private KycProfileRepository kycProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private KycService kycService;

    private User mockUser;
    private KycProfile mockProfile;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).isKyc(false).build();
        mockProfile = new KycProfile();
        mockProfile.setId(1L);
        mockProfile.setUser(mockUser);
        mockProfile.setStatus(Status.PENDING);
    }

    @Test
    void approveKyc_ShouldConfirmAndCreateAccount_WhenApproved() {
        KycApprovalRequest request = new KycApprovalRequest();
        request.setStatus("CONFIRM");
        request.setFullName("Nguyen Van Test");

        when(kycProfileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(accountRepository.findByUserId(1L)).thenReturn(new ArrayList<>()); // User chưa có tài khoản
        when(kycProfileRepository.save(any(KycProfile.class))).thenReturn(mockProfile);

        KycResponse response = kycService.approveKyc(1L, request);

        assertEquals("CONFIRM", response.getStatus());
        assertTrue(mockUser.getIsKyc());
        verify(userRepository, times(1)).save(mockUser);
        verify(accountRepository, times(1)).save(any(Account.class)); // Đảm bảo tài khoản mới được sinh ra
    }
}