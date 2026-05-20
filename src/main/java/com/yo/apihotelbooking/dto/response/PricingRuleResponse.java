package com.yo.apihotelbooking.dto.response;
import com.yo.apihotelbooking.schemas.enums.PricingRuleType;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data

public class PricingRuleResponse {

    private String ruleName;
    private PricingRuleType ruleType;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal priceModifier;
    private BigDecimal pricePercent;

    private Integer priority;
}