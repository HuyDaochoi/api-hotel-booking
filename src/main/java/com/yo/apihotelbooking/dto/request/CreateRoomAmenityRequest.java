package com.yo.apihotelbooking.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateRoomAmenityRequest {
    @NotNull(message = "Room type ID cannot be null")
    private Long roomTypeId;

    @NotBlank(message = "Amenity name cannot be blank")
    private String amenityName;

    private String iconCode;
}