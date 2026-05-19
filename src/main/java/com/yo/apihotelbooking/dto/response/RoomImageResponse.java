package com.yo.apihotelbooking.dto.response;

import lombok.Data;

@Data
public class RoomImageResponse {
    private Long id;
    private String imageUrl;
    private String caption;
    private Boolean isPrimary;
}