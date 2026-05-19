package com.yo.apihotelbooking.dto.response;

import lombok.Data;

@Data
public class RoomAmenityResponse {
    private Long id;
    private String amenityName;
    private String iconCode;
}