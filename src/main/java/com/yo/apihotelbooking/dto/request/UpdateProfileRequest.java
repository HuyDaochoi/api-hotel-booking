package com.yo.apihotelbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{8,10}$",
             message = "Số điện thoại không hợp lệ (VD: 0901234567)")
    private String phone;
}