package com.example.rikkeibank.config;

import com.example.rikkeibank.model.entity.Account;
import com.example.rikkeibank.model.entity.Role;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.AccountRepository;
import com.example.rikkeibank.repository.RoleRepository;
import com.example.rikkeibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Tự động tạo các Role nền tảng nếu hệ thống chưa có
        if (roleRepository.findByName("ROLE_CUSTOMER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Quản trị viên").build());
            roleRepository.save(Role.builder().name("ROLE_STAFF").description("Giao dịch viên").build());
            roleRepository.save(Role.builder().name("ROLE_CUSTOMER").description("Khách hàng").build());
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElse(null);

        // 2. Tự động tạo hoặc đồng bộ lại thông tin User 1 (nguyenvana)
        User user1 = userRepository.findByUsername("nguyenvana").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("nguyenvana")
                        .password(passwordEncoder.encode("password123"))
                        .email("nva@gmail.com")
                        .phoneNumber("0987654321")
                        .role(customerRole)
                        .isActive(true)
                        .isKyc(false)
                        .build())
        );

        // Tạo hoặc cập nhật mã PIN chuẩn xác bằng Java mã hóa cho Tài khoản 111111
        if (accountRepository.findByAccountNumber("111111").isEmpty()) {
            accountRepository.save(Account.builder()
                    .accountNumber("111111")
                    .balance(new BigDecimal("5000000"))
                    .currency("VND")
                    .transactionPin(passwordEncoder.encode("123456")) // Mã hóa trực tiếp bằng BCrypt Java
                    .active(true)
                    .user(user1)
                    .build());
        } else {
            Account acc1 = accountRepository.findByAccountNumber("111111").get();
            acc1.setTransactionPin(passwordEncoder.encode("123456"));
            accountRepository.save(acc1);
        }

        // 3. Tự động tạo hoặc đồng bộ lại thông tin User 2 (tranvanb)
        User user2 = userRepository.findByUsername("tranvanb").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("tranvanb")
                        .password(passwordEncoder.encode("password123"))
                        .email("tvb@gmail.com")
                        .phoneNumber("0123456789")
                        .role(customerRole)
                        .isActive(true)
                        .isKyc(false)
                        .build())
        );

        // Tạo hoặc cập nhật mã PIN chuẩn xác bằng Java mã hóa cho Tài khoản 222222
        if (accountRepository.findByAccountNumber("222222").isEmpty()) {
            accountRepository.save(Account.builder()
                    .accountNumber("222222")
                    .balance(new BigDecimal("0"))
                    .currency("VND")
                    .transactionPin(passwordEncoder.encode("123456"))
                    .active(true)
                    .user(user2)
                    .build());
        } else {
            Account acc2 = accountRepository.findByAccountNumber("222222").get();
            acc2.setTransactionPin(passwordEncoder.encode("123456"));
            accountRepository.save(acc2);
        }
    }
}