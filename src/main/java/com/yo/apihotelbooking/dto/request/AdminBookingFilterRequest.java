package com.yo.apihotelbooking.dto.request;

import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class AdminBookingFilterRequest {

    private BookingStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    private Long roomId;
    private Long userId;

    private int page    = 0;
    private int size    = 20;

    private String sortBy  = "createdAt";
    private String sortDir = "desc";
}