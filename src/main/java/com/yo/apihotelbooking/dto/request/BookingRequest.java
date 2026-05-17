package com.yo.apihotelbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "Room ID là bắt buộc")
    private Long roomId;

    @NotNull(message = "Ngày nhận phòng là bắt buộc")
    @FutureOrPresent(message = "Ngày nhận phòng không thể ở quá khứ")
    private LocalDate checkInDate;

    @NotNull(message = "Ngày trả phòng là bắt buộc")
    @Future(message = "Ngày trả phòng phải sau ngày hiện tại")
    private LocalDate checkOutDate;

    private String specialRequests;

    @Min(value = 1, message = "Số khách phải ít nhất là 1")
    @Max(value = 20, message = "Số khách không được vượt quá 20")
    private Integer numGuests;
}
