package com.yo.apihotelbooking.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.yo.apihotelbooking.schemas.domain.Room;
import com.yo.apihotelbooking.schemas.domain.RoomAmenities;
import com.yo.apihotelbooking.schemas.domain.RoomImages;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class RoomTypeResponse {

    private String name;

    private String description;

    private BigDecimal basePrice;

    private Integer maxCapacity;

    private String cancellationPolicy;

    private Boolean isActive = true;

}