package com.example.rikkeibank.service;

import com.example.rikkeibank.model.dto.request.LoginRequest;
import com.example.rikkeibank.model.dto.request.RegisterRequest;
import com.example.rikkeibank.model.dto.request.ResetPasswordRequest;
import com.example.rikkeibank.model.dto.response.TokenResponse;
import com.example.rikkeibank.model.entity.RefreshToken;
import com.example.rikkeibank.model.entity.Role;
import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.RoleRepository;
import com.example.rikkeibank.repository.UserRepository;
import com.example.rikkeibank.security.CustomUserDetails;
import com.example.rikkeibank.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        Role userRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role."));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(userRole)
                .isActive(true)
                .isKyc(false)
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String jwt = jwtProvider.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId()).getToken();

        return TokenResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    CustomUserDetails userDetails = CustomUserDetails.build(user);
                    String token = jwtProvider.generateAccessToken(userDetails);
                    return TokenResponse.builder()
                            .accessToken(token)
                            .refreshToken(requestRefreshToken)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong cơ sở dữ liệu!"));
    }

    @Transactional
    public void logout(String jwt, Long userId) {
        long remainingTime = jwtProvider.getRemainingTimeFromToken(jwt);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set("BL_" + jwt, "revoked", remainingTime, TimeUnit.MILLISECONDS);
        }
        refreshTokenService.deleteByUserId(userId);
    }

    // ==========================================
    // TÍNH NĂNG QUÊN MẬT KHẨU
    // ==========================================

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));

        // Sinh mã OTP 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Lưu vào Redis, TTL = 5 phút. Key có tiền tố "OTP_"
        redisTemplate.opsForValue().set("OTP_" + email, otp, 5, TimeUnit.MINUTES);

        // Mô phỏng việc gửi Email (Trong thực tế sẽ dùng JavaMailSender ở đây)
        log.info("=====================================================");
        log.info("Mã OTP khôi phục mật khẩu cho email [{}] là: {}", email, otp);
        log.info("Mã OTP sẽ hết hạn sau 5 phút.");
        log.info("=====================================================");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Lấy OTP từ Redis ra kiểm tra
        String cachedOtp = redisTemplate.opsForValue().get("OTP_" + request.getEmail());
        if (cachedOtp == null) {
            throw new RuntimeException("Mã OTP đã hết hạn hoặc không tồn tại.");
        }
        if (!cachedOtp.equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác.");
        }

        // 2. Lấy User và cập nhật mật khẩu mới
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 3. Xóa OTP khỏi Redis sau khi đổi mk thành công (Bảo mật 1 lần dùng)
        redisTemplate.delete("OTP_" + request.getEmail());
    }
}