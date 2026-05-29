package com.yo.apihotelbooking.dto.response;

import com.yo.apihotelbooking.schemas.enums.BookingPaymentStatus;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookingResponse {

    private Long id;

    private UserResponse user;
    private RoomResponse room;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numGuests;
    private BookingStatus status;
    private String cancellationReason;
    private String lastStatusNote;
    private BookingPaymentStatus bookingPaymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

}