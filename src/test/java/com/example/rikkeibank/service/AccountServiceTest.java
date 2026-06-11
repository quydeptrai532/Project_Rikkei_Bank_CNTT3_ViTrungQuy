package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.CreateAccountRequest;
import com.example.rikkeibank.model.dto.response.AccountBalanceResponse;
import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.AccountRepository;
import com.example.rikkeibank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).username("testuser").build();
    }

    @Test
    void openNewAccount_ShouldThrowException_WhenLimitReached() {
        List<Account> existingAccounts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            existingAccounts.add(new Account());
        }

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(accountRepository.findByUserId(1L)).thenReturn(existingAccounts);

        CreateAccountRequest request = new CreateAccountRequest();
        request.setTransactionPin("123456");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.openNewAccount(1L, request);
        });

        assertEquals("Quý khách đã đạt giới hạn mở tối đa 5 tài khoản.", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void openNewAccount_ShouldSuccess_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(accountRepository.findByUserId(1L)).thenReturn(new ArrayList<>()); // 0 account
        when(passwordEncoder.encode("123456")).thenReturn("encodedPin");

        Account savedAccount = Account.builder()
                .accountNumber("9123456789")
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        CreateAccountRequest request = new CreateAccountRequest();
        request.setTransactionPin("123456");

        AccountBalanceResponse response = accountService.openNewAccount(1L, request);

        assertNotNull(response);
        assertEquals("9123456789", response.getAccountNumber());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}