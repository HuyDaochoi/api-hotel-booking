package com.yo.apihotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueResponse {
    private String month;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefund;
    private BigDecimal netRevenue;
    private Long transactionCount;
}