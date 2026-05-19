package com.yo.apihotelbooking.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateRoomImageRequest {
    @NotNull(message = "Room type ID cannot be null")
    private Long roomTypeId;

    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;

    private String caption;
    private Boolean isPrimary = false;
    private Integer sortOrder = 0;
}