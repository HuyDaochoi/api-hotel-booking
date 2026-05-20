package com.yo.apihotelbooking.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateRoomImageRequest {
    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;
    
    private String caption;
    private Boolean isPrimary;
    private Integer sortOrder;
}