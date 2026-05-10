package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.schemas.domain.PricingRule;
import com.yo.apihotelbooking.services.PricingService; // Giả định bạn để logic xử lý CRUD ở đây

import jakarta.validation.Valid;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.PricingRuleRequest;
import com.yo.apihotelbooking.dto.response.PricingRuleResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing-rules")
@RequiredArgsConstructor
public class AdminPricingRuleController {

    private final PricingService pricingService;

   @GetMapping
    public ApiResponse<List<PricingRuleResponse>> getAllRules() {
        return ApiResponse.success("Lấy danh sách thành công", pricingService.getAllRules());
    }
    @PostMapping
    public ApiResponse<PricingRuleResponse> createRule(@Valid @RequestBody PricingRuleRequest request) throws Exception {
        return ApiResponse.success("Tạo quy tắc mới thành công", pricingService.createRule(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PricingRuleResponse> updateRule(
            @PathVariable Long id, 
            @Valid @RequestBody PricingRuleRequest request) throws Exception {
        return ApiResponse.success("Cập nhật thành công", pricingService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        pricingService.deleteRule(id);
        return ApiResponse.success("Xóa thành công", null);
    }
}