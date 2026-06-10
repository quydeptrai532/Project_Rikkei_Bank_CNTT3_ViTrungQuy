package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.response.AccountBalanceResponse;
import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountBalanceResponse getBalance(Long currentUserId) {
        // Lấy tài khoản đầu tiên được kích hoạt của người dùng hiện tại
        Account account = accountRepository.findByUserId(currentUserId).stream()
                .filter(Account::getActive)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tài khoản ngân hàng không tồn tại hoặc đã bị khóa."));

        return AccountBalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }
}