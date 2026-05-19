package com.yo.apihotelbooking.dto.response;

import lombok.Data;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class RoomTypeResponse {
    private Long id;

    private String name;

    private String description;

    private BigDecimal basePrice;

    private Integer maxCapacity;

    private String cancellationPolicy;

    private Boolean isActive = true;
    private List<RoomImageResponse> images;
    private List<RoomAmenityResponse> amenities;

}