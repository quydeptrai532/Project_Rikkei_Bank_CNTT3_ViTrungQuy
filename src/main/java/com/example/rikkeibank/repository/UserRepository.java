package com.example.rikkeibank.repository;

import com.example.rikkeibank.model.dto.response.UserResponseDto;
import com.example.rikkeibank.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    // UC-02: Truy vấn trực tiếp ra DTO để tối ưu RAM
    @Query("SELECT new com.example.rikkeibank.model.dto.response.UserResponseDto(" +
            "u.id, u.username, u.email, u.phoneNumber, u.isKyc, r.name) " +
            "FROM User u JOIN u.role r")
    Page<UserResponseDto> findAllUsersWithProjection(Pageable pageable);
}