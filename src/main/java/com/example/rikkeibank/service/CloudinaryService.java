package com.example.rikkeibank.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // Upload file lên Cloudinary, đặt folder là "rikkei_bank_kyc" cho gọn gàng
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "rikkei_bank_kyc"));

            // Lấy ra chuỗi URL bảo mật (https)
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải file lên hệ thống Cloudinary: " + e.getMessage());
        }
    }
}