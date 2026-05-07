package com.yo.apihotelbooking.dto.request;
import jakarta.validation.constraints.NotNull;

import com.yo.apihotelbooking.schemas.domain.RoomType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotNull
    private Long roomTypeId;
    @NotBlank
    private String roomNumber;

    private Integer floor;

    private String description;
}
