package com.example.rikkeibank.service;

import com.example.rikkeibank.exception.InsufficientBalanceException;
import com.example.rikkeibank.model.dto.request.TransferRequest;
import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.model.entity.Transaction;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.AccountRepository;
import com.example.rikkeibank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionService transactionService;

    private Account fromAccount;
    private Account toAccount;
    private TransferRequest request;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Khởi tạo đối tượng khách hàng sở hữu tài khoản
        mockUser = User.builder()
                .id(1L)
                .username("nguyenvana")
                .build();

        fromAccount = Account.builder()
                .id(1L)
                .accountNumber("SRC123")
                .balance(new BigDecimal("500000"))
                .transactionPin("encodedPin")
                .active(true)
                .user(mockUser) // Gắn chủ sở hữu vào tài khoản nguồn
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountNumber("DEST456")
                .balance(new BigDecimal("100000"))
                .active(true)
                .build();

        request = new TransferRequest();
        request.setFromAccountNumber("SRC123");
        request.setTargetAccountNumber("DEST456");
        request.setAmount(new BigDecimal("100000"));
        request.setTransactionPin("123456");
        request.setDescription("Chuyển tiền phòng máy test");
    }

    @Test
    void performTransfer_ShouldThrowException_WhenInsufficientBalance() {
        fromAccount.setBalance(new BigDecimal("50000")); // Hạ số dư xuống thấp hơn mức chuyển

        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("DEST456")).thenReturn(Optional.of(toAccount));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);

        Exception exception = assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.performTransfer(1L, request);
        });

        assertEquals("Số dư khả dụng của quý khách không đủ để thực hiện giao dịch.", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void performTransfer_ShouldSuccess_WhenValidData() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("DEST456")).thenReturn(Optional.of(toAccount));
        when(passwordEncoder.matches("123456", "encodedPin")).thenReturn(true);

        Transaction mockTx = new Transaction();
        mockTx.setTransactionCode("TXN123");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTx);

        Transaction result = transactionService.performTransfer(1L, request);

        assertEquals(new BigDecimal("400000"), fromAccount.getBalance());
        assertEquals(new BigDecimal("200000"), toAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertNotNull(result);
        assertEquals("TXN123", result.getTransactionCode());
    }
}