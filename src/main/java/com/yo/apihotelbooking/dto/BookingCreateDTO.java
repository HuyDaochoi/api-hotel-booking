package com.yo.apihotelbooking.dto;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
@Data
public class BookingCreateDTO {

    @NotNull(message = "userId không được null")
    private Long userId;

    @NotNull(message = "roomId không được null")
    private Long roomId;

    @NotNull(message = "checkIn không được null")
    private LocalDate checkInDate;

    @NotNull(message = "checkOut không được null")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Ít nhất 1 khách")
    private Integer numGuests;

    private String specialRequests;
}