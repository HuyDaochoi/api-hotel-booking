package com.yo.apihotelbooking.dto.response;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    private BigDecimal totalAmount;

    private String specialRequests;

    private LocalDateTime createdAt;
}
