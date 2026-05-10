package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.response.PricingEstimateResponse;
import com.yo.apihotelbooking.services.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {
    private final PricingService pricingService;

    @GetMapping("/estimate")
    public ApiResponse<PricingEstimateResponse> getPriceEstimate(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) throws NotFoundException {
        
        PricingEstimateResponse estimate = pricingService.estimatePrice(roomId, checkIn, checkOut);
        return ApiResponse.success("Lấy thông tin ước tính giá thành công", estimate);
    }
}