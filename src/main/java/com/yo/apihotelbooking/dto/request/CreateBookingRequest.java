package com.yo.apihotelbooking.dto.request;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateBookingRequest {

    @NotNull(message = "userId không được null")
    private Long userId;

    @NotNull(message = "roomId không được null")
    private Long roomId;

    @NotNull(message = "checkInDate không được null")
    private LocalDate checkInDate;

    @NotNull(message = "checkOutDate không được null")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Ít nhất 1 khách")
    private Integer numGuests;

    private String specialRequests;
}
