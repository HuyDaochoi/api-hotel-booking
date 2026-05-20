package com.yo.apihotelbooking.dto.request;

import com.yo.apihotelbooking.schemas.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255)
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{8,10}$",
             message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotNull(message = "Role không được để trống")
    private UserRole role;

    @NotNull(message = "isActive không được để trống")
    private Boolean isActive;
}