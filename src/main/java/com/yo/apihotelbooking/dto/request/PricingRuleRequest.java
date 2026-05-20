package com.yo.apihotelbooking.dto.request;

import com.yo.apihotelbooking.schemas.enums.PricingRuleType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PricingRuleRequest {
    private Long id; 
    private Long roomTypeId; 

    @NotBlank(message = "Tên quy tắc không được để trống")
    private String ruleName;

    @NotNull(message = "Loại quy tắc là bắt buộc")
    private PricingRuleType ruleType; 

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal priceModifier; 
    private BigDecimal pricePercent;  

    @Min(1)
    private Integer minNights = 1;

    @NotNull
    private Integer priority = 0;

    private Boolean isActive = true;
}