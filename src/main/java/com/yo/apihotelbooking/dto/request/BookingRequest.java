package com.yo.apihotelbooking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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
    private Integer numGuests;
}