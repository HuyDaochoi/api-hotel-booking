package com.yo.apihotelbooking.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class UserResponse {

    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private String role;
    @JsonProperty("isActive")
    private Boolean isActive;
}