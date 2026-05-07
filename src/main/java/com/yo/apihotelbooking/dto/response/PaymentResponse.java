package com.yo.apihotelbooking.dto.response;
import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class PaymentResponse {

    private Long id;

    private BigDecimal amount;

    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    private String transactionRef;

    private LocalDateTime processedAt;
}
