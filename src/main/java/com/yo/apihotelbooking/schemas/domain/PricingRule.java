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
public class PricingRule extends AuditableEntity {
    
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type") 
    private PricingRuleType ruleType;

    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "price_modifier", precision = 12, scale = 2)   
    private BigDecimal priceModifier;
    
    @Column(name = "price_percent", precision = 5, scale = 2)
    private BigDecimal pricePercent;
    
    @Column(name = "min_nights")
    private Integer minNights;
    
    @Column(name = "priority")
    private Integer priority;

    @Column(name = "is_active")
    private Boolean isActive = true;
}