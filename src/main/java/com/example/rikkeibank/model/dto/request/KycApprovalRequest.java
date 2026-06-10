package com.example.rikkeibank.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KycApprovalRequest {
    @NotBlank(message = "Trạng thái phê duyệt không được để trống")
    @Pattern(regexp = "CONFIRM|REJECT", message = "Trạng thái phải là CONFIRM hoặc REJECT")
    private String status;

    // Giao dịch viên sẽ kiểm tra ảnh CCCD và nhập/xác nhận thông tin chính xác vào hệ thống
    private String idNumber;
    private String fullName;
    private String dob; // Định dạng chuỗi YYYY-MM-DD từ client gửi lên
    private String sex;
    private String address;
}