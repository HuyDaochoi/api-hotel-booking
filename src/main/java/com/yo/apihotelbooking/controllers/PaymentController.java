package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.PaymentFilterRequest;
import com.yo.apihotelbooking.dto.response.PaymentResponse;
import com.yo.apihotelbooking.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/api/bookings/{bookingId}/payments")
    public ApiResponse<List<PaymentResponse>> getPaymentsByBooking(
            @PathVariable Long bookingId) {
        return ApiResponse.success(
                "Lấy danh sách thanh toán của đơn thành công",
                paymentService.getPaymentsByBooking(bookingId));
    }

    // Admin/Staff xem toàn bộ giao dịch với filter + phân trang
    @GetMapping("/api/admin/payments")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<Page<PaymentResponse>> getAllPayments(
            @ModelAttribute PaymentFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(
                "Lấy danh sách toàn bộ giao dịch thành công",
                paymentService.getAllPaymentsForAdmin(filterRequest, page, size));
    }

    // Admin/Staff xem chi tiết 1 giao dịch
    @GetMapping("/api/admin/payments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<PaymentResponse> getPaymentDetail(@PathVariable Long id) {
        return ApiResponse.success(
                "Lấy thông tin chi tiết giao dịch thành công",
                paymentService.getPaymentDetail(id));
    }
}