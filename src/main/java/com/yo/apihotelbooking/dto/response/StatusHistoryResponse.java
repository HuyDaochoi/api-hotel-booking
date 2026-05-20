package com.yo.apihotelbooking.dto.response;

import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StatusHistoryResponse {

    private Long            id;
    private BookingStatus   oldStatus;
    private BookingStatus   newStatus;
    private Long            changedById;
    private String          changedByName;

    private String          note;
    private LocalDateTime   changedAt;
}