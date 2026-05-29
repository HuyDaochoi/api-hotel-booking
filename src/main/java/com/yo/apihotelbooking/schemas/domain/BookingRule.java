package com.yo.apihotelbooking.schemas.domain;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_rules")
@Getter @Setter
public class BookingRule extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;
    @Column(name = "min_days_advance")
    private Integer minDaysAdvance = 0;
    private String ruleName;
    private Integer minNights;
    private Integer maxNights;
    private BigDecimal depositPercentage;
    private Boolean isActive;
}