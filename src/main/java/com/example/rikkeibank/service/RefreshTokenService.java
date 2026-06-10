package com.example.rikkeibank.service;

import com.example.rikkeibank.model.entity.RefreshToken;
import com.example.rikkeibank.repository.RefreshTokenRepository;
import com.example.rikkeibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.expiration.refresh-token}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Tạo refresh token mới (mỗi user chỉ có 1 token active)
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {

        // ⚠️ Xóa token cũ - phải trong transaction
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(
                        userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"))
                )
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Kiểm tra token hết hạn hoặc bị revoke
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(Instant.now()) || token.getRevoked()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn hoặc bị thu hồi. Vui lòng đăng nhập lại.");
        }

        return token;
    }

    /**
     * Tìm token theo chuỗi
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Xóa token theo userId (nếu gọi riêng)
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}