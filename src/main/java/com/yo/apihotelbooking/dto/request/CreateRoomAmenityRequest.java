package com.yo.apihotelbooking.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateRoomAmenityRequest {

    @NotBlank(message = "Amenity name cannot be blank")
    private String amenityName;

    private String iconCode;
}