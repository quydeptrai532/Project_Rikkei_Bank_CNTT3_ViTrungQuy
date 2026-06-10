package com.example.rikkeibank.security;

import com.example.rikkeibank.model.entity.User;
import com.example.rikkeibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ FIX LỖI LAZY LOADING
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

        // Khi có @Transactional, đoạn code lấy Role bên trong hàm build() dưới đây sẽ hoạt động bình thường
        return CustomUserDetails.build(user);
    }
}