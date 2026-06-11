package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.CreateAccountRequest;
import com.example.rikkeibank.model.dto.response.AccountBalanceResponse;
import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.AccountRepository;
import com.example.rikkeibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Lấy danh sách TẤT CẢ tài khoản của User hiện tại
    public List<AccountBalanceResponse> getAllMyAccounts(Long currentUserId) {
        List<Account> accounts = accountRepository.findByUserId(currentUserId);

        return accounts.stream()
                .filter(Account::getActive)
                .map(acc -> AccountBalanceResponse.builder()
                        .accountNumber(acc.getAccountNumber())
                        .balance(acc.getBalance())
                        .currency(acc.getCurrency())
                        .build())
                .collect(Collectors.toList());
    }

    // Nghiệp vụ: Khách hàng chủ động mở thêm tài khoản mới
    @Transactional
    public AccountBalanceResponse openNewAccount(Long currentUserId, CreateAccountRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng."));

        // Ràng buộc nghiệp vụ: Tối đa 5 tài khoản / 1 người dùng
        long currentAccountCount = accountRepository.findByUserId(currentUserId).size();
        if (currentAccountCount >= 5) {
            throw new IllegalArgumentException("Quý khách đã đạt giới hạn mở tối đa 5 tài khoản.");
        }

        Account newAccount = Account.builder()
                .accountNumber(generateRandomAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .transactionPin(passwordEncoder.encode(request.getTransactionPin()))
                .active(true)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(newAccount);

        return AccountBalanceResponse.builder()
                .accountNumber(savedAccount.getAccountNumber())
                .balance(savedAccount.getBalance())
                .currency(savedAccount.getCurrency())
                .build();
    }

    // Hàm sinh số tài khoản ngẫu nhiên (10 số, bắt đầu bằng 9)
    private String generateRandomAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append("9");
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}