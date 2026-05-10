package com.yo.apihotelbooking.schemas.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.yo.apihotelbooking.schemas.AuditableEntity;
import com.yo.apihotelbooking.schemas.enums.PricingRuleType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "pricing_rules")
@Getter @Setter
public class PricingRule  extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(name = "rule_name")
    private String ruleName;
    private Integer dayOfWeek;
    @Enumerated(EnumType.STRING)
    private PricingRuleType ruleType;

    private LocalDate startDate;
    private LocalDate endDate;
    @Column(precision = 12, scale = 2)   
    private BigDecimal priceModifier;
    @Column(precision = 5, scale = 2)
    private BigDecimal pricePercent;
    
    private Integer minNights;
    private Integer priority;

    private Boolean isActive;
}
