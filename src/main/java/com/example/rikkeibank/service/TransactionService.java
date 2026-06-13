package com.example.rikkeibank.service;

import com.example.rikkeibank.exception.BadRequestException;
import com.example.rikkeibank.exception.InsufficientBalanceException;
import com.example.rikkeibank.exception.ResourceNotFoundException;
import com.example.rikkeibank.model.dto.request.TransferRequest;
import com.example.rikkeibank.model.dto.response.TransactionHistoryResponse;
import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.model.entity.Transaction;
import com.example.rikkeibank.repository.AccountRepository;
import com.example.rikkeibank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    // UC-04: Xử lý giao dịch chuyển tiền
    @Transactional
    public Transaction performTransfer(Long currentUserId, TransferRequest request) {
        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản nguồn không tồn tại trên hệ thống."));

        if (!fromAccount.getUser().getIsKyc()) {
            throw new BadRequestException("Tài khoản chưa được định danh (KYC). Vui lòng thực hiện KYC để chuyển tiền.");
        }

        if (!fromAccount.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("Giao dịch bị từ chối: Bạn không có quyền sử dụng tài khoản nguồn này.");
        }

        if (!fromAccount.getActive()) {
            throw new BadRequestException("Tài khoản nguồn hiện đang bị khóa hoặc tạm dừng giao dịch.");
        }

        Account toAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản đích không tồn tại trên hệ thống."));

        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new BadRequestException("Không thể thực hiện giao dịch chuyển tiền cho chính số tài khoản này.");
        }

        if (fromAccount.getTransactionPin() == null || !passwordEncoder.matches(request.getTransactionPin(), fromAccount.getTransactionPin())) {
            throw new IllegalArgumentException("Mã PIN giao dịch không chính xác. Vui lòng kiểm tra lại.");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Số dư khả dụng của quý khách không đủ để thực hiện giao dịch.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .transactionCode("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();

        return transactionRepository.save(transaction);
    }

    // UC-06: Xem lịch sử sao kê
    public Page<TransactionHistoryResponse> getTransactionHistory(Long currentUserId, String accountNumber, Pageable pageable) {
        // Lấy đúng số tài khoản mà Client truyền lên
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản: " + accountNumber));

        // Bảo mật: Kiểm tra xem tài khoản này có thuộc về người đang đăng nhập không
        if (!account.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("Bạn không có quyền truy cập lịch sử của tài khoản này.");
        }

        Page<Transaction> transactions = transactionRepository.findTransactionsByAccountId(account.getId(), pageable);

        return transactions.map(t -> {
            boolean isSender = t.getFromAccount().getId().equals(account.getId());

            String type = isSender ? "Giao dịch trừ tiền - DEBIT" : "Giao dịch cộng tiền - CREDIT";
            String relatedAcc = isSender ? t.getToAccount().getAccountNumber() : t.getFromAccount().getAccountNumber();

            return TransactionHistoryResponse.builder()
                    .transactionCode(t.getTransactionCode())
                    .transactionType(type)
                    .amount(t.getAmount())
                    .relatedAccountNumber(relatedAcc)
                    .description(t.getDescription())
                    .status(t.getStatus())
                    .createdAt(t.getCreatedAt())
                    .build();
        });
    }
}