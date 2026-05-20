package com.yo.apihotelbooking.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
public class PricingEstimateResponse {
    private Long roomId;
    private BigDecimal totalAmount;
    private List<NightlyPriceBreakdown> breakdown;

    @Data
    
    public static class NightlyPriceBreakdown {
        private LocalDate date;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        private String appliedRuleName;
    }
}