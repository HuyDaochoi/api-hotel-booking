package com.yo.apihotelbooking.dto.request;
import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreatePaymentRequest {

    @NotNull(message = "bookingId không được null")
    private Long bookingId;

    @NotNull(message = "amount không được null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount phải > 0")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod không được null")
    private PaymentMethod paymentMethod;
}