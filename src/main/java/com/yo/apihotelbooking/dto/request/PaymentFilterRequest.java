package com.yo.apihotelbooking.dto.request;

import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentFilterRequest {
    private Long bookingId;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    private BigDecimal amountPaid = BigDecimal.ZERO;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}