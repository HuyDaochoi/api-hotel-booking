package com.yo.apihotelbooking.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RoomSearchRequest {
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestCount; 
    private List<Long> amenityIds; 
}