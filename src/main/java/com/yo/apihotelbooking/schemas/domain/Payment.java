package com.yo.apihotelbooking.schemas.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.yo.apihotelbooking.schemas.AuditableEntity;
import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "payments")
@Getter @Setter
public class Payment extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(columnDefinition = "Varchar(100)")
    private String transactionRef;
    @Column(columnDefinition = "text")
    private String note;

    private LocalDateTime processedAt;
}
