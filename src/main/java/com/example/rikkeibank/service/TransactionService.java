package com.example.rikkeibank.service;

import com.example.rikkeibank.exception.BadRequestException;
import com.example.rikkeibank.exception.InsufficientBalanceException;
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

    // UC-04: Xử lý giao dịch chuyển tiền giữa hai tài khoản bất kỳ được chỉ định cụ thể
    @Transactional
    public Transaction performTransfer(Long currentUserId, TransferRequest request) {
        // 1. Tìm tài khoản nguồn theo số tài khoản truyền lên từ Client
        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new RuntimeException("Tài khoản nguồn không tồn tại trên hệ thống."));
        // Check xem User đã KYC chưa
        if (!fromAccount.getUser().getIsKyc()) {
            throw new BadRequestException("Tài khoản chưa được định danh (KYC). Vui lòng thực hiện KYC để chuyển tiền.");
        }
        // BẢO MẬT CỐT LÕI: Kiểm tra tài khoản nguồn có thực sự thuộc về User đang đăng nhập không
        if (!fromAccount.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Giao dịch bị từ chối: Bạn không có quyền sử dụng tài khoản nguồn này.");
        }

        // Kiểm tra trạng thái hoạt động của tài khoản nguồn
        if (!fromAccount.getActive()) {
            throw new RuntimeException("Tài khoản nguồn hiện đang bị khóa hoặc tạm dừng giao dịch.");
        }

        // 2. Tìm tài khoản thụ hưởng theo số tài khoản đích nhập vào
        Account toAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new RuntimeException("Tài khoản đích không tồn tại trên hệ thống."));

        // Kiểm tra tránh việc tự chuyển tiền cho cùng một số tài khoản
        if (fromAccount.getAccountNumber().equals(toAccount.getAccountNumber())) {
            throw new RuntimeException("Không thể thực hiện giao dịch chuyển tiền cho chính số tài khoản này.");
        }

        // 3. Xác thực mã PIN giao dịch của tài khoản nguồn bằng BCrypt
        if (fromAccount.getTransactionPin() == null || !passwordEncoder.matches(request.getTransactionPin(), fromAccount.getTransactionPin())) {
            throw new IllegalArgumentException("Mã PIN giao dịch không chính xác. Vui lòng kiểm tra lại.");
        }

        // 4. Ràng buộc kiểm tra số dư phòng chống rủi ro Double-spending
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Số dư khả dụng của quý khách không đủ để thực hiện giao dịch.");
        }

        // 5. Khấu trừ tài khoản nguồn và cộng tiền vào tài khoản đích
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 6. Khởi tạo bản ghi lịch sử giao dịch kiểm toán
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

    // UC-06: Xem lịch sử sao kê tổng hợp các tài khoản của người dùng
    public Page<TransactionHistoryResponse> getTransactionHistory(Long currentUserId, Pageable pageable) {
        Account account = accountRepository.findByUserId(currentUserId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản của bạn."));

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