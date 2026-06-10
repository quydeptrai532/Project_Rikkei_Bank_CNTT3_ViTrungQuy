package com.example.rikkeibank.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idNumber; // Số CCCD/CMND

    private String fullName;
    private LocalDate dob;
    private String sex;
    private String address;
    private String idCardFrontUrl; // URL lưu trên Cloudinary

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, CONFIRM, REJECT

    private LocalDateTime verifiedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}