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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // Tiêm thêm 2 bean này để xử lý việc tạo Account và băm mã PIN
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public KycResponse uploadKyc(MultipartFile file, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        String imageUrl = cloudinaryService.uploadFile(file);

        KycProfile kycProfile = kycProfileRepository.findByUserId(userId)
                .orElse(new KycProfile());

        kycProfile.setUser(user);
        kycProfile.setIdCardFrontUrl(imageUrl);
        kycProfile.setStatus(Status.PENDING);

        KycProfile savedProfile = kycProfileRepository.save(kycProfile);

        return mapToKycResponse(savedProfile);
    }

    @Transactional
    public KycResponse approveKyc(Long profileId, KycApprovalRequest request) {
        KycProfile kycProfile = kycProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ định danh với ID: " + profileId));

        Status newStatus = Status.valueOf(request.getStatus());
        kycProfile.setStatus(newStatus);
        kycProfile.setVerifiedAt(LocalDateTime.now());

        if (newStatus == Status.CONFIRM) {
            kycProfile.setIdNumber(request.getIdNumber());
            kycProfile.setFullName(request.getFullName());
            if (request.getDob() != null && !request.getDob().isBlank()) {
                kycProfile.setDob(LocalDate.parse(request.getDob()));
            }
            kycProfile.setSex(request.getSex());
            kycProfile.setAddress(request.getAddress());

            User user = kycProfile.getUser();
            user.setIsKyc(true);
            userRepository.save(user);

            // =========================================================================
            // NGHIỆP VỤ LÕI: TỰ ĐỘNG TẠO TÀI KHOẢN NGÂN HÀNG KHI DUYỆT KYC THÀNH CÔNG
            // =========================================================================
            boolean hasAccount = accountRepository.findByUserId(user.getId()).stream().findAny().isPresent();

            // Nếu người dùng chưa có tài khoản nào thì mới tạo
            if (!hasAccount) {
                Account newAccount = Account.builder()
                        .accountNumber(generateRandomAccountNumber()) // Sinh số tài khoản ngẫu nhiên
                        .balance(BigDecimal.ZERO) // Tiền khởi tạo là 0đ
                        .currency("VND")
                        .transactionPin(passwordEncoder.encode("123456")) // Mã PIN mặc định là 123456
                        .active(true)
                        .user(user)
                        .build();
                accountRepository.save(newAccount);
            }

        } else if (newStatus == Status.REJECT) {
            User user = kycProfile.getUser();
            user.setIsKyc(false);
            userRepository.save(user);
        }

        KycProfile updatedProfile = kycProfileRepository.save(kycProfile);
        return mapToKycResponse(updatedProfile);
    }

    private KycResponse mapToKycResponse(KycProfile profile) {
        return KycResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .idNumber(profile.getIdNumber())
                .status(profile.getStatus().name())
                .idCardFrontUrl(profile.getIdCardFrontUrl())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    // Hàm tiện ích: Sinh ngẫu nhiên số tài khoản gồm 10 chữ số
    private String generateRandomAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        // Để cho giống số tài khoản thật, ta bắt đầu bằng số 9
        sb.append("9");
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}