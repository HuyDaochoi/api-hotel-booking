package com.yo.apihotelbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    
    @NotBlank(message = "Tên quyền không được để trống")
    private String name;
    
    private String description;
}