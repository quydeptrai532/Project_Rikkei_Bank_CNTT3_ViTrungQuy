package com.example.rikkeibank.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionCode; // Mã giao dịch (VD: TXN123456)

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String description;

    private String status; // VD: SUCCESS, FAILED

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Tài khoản nguồn (Người chuyển)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    // Tài khoản đích (Người nhận)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;
}