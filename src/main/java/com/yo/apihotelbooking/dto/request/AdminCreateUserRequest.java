package com.yo.apihotelbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminCreateUserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255)
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{8,10}$",
             message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotNull(message = "Role không được để trống")
    private String role ;
}