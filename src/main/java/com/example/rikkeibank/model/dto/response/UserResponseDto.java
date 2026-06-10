package com.example.rikkeibank.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor // Phục vụ cho JPQL new com.example.rikkeibank.dto.response.UserResponseDto(...)
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean isKyc;
    private String roleName;
}