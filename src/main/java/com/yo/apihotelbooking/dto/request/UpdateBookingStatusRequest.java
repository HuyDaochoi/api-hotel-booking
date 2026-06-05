package com.yo.apihotelbooking.dto.request;

import com.yo.apihotelbooking.schemas.enums.BookingPaymentStatus;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;

import lombok.Data;
@Data
public class UpdateBookingStatusRequest {
    private BookingStatus status;
    private BookingPaymentStatus bookingPaymentStatus;
    private String note;
}
