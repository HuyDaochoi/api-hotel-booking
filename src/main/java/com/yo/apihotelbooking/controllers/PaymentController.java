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
@PreAuthorize("hasAnyRole('ADMIN','STAFF') or (hasRole('CUSTOMER') and @securityUtils.isBookingOwner(#bookingId))") 
public class PaymentController {

    private final PaymentService paymentService;

    // 1. Dành cho cả Khách hàng (Chính chủ) và Admin/Staff kiểm tra theo mã Booking công khai
    @GetMapping("/api/bookings/{bookingId}/payments")
    public ApiResponse<List<PaymentResponse>> getPaymentsByBooking(@PathVariable Long bookingId) {
        return ApiResponse.success(
                "Lấy danh sách thanh toán của đơn thành công",
                paymentService.getPaymentsByBooking(bookingId)
        );
    }

    // 2. [ADMIN/STAFF Only] Xem toàn hệ thống thanh toán kết hợp bộ lọc nâng cao và phân trang
    @GetMapping("/api/admin/payments")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<Page<PaymentResponse>> getAllPayments(
            PaymentFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(
                "Lấy danh sách toàn bộ giao dịch hệ thống thành công",
                paymentService.getAllPaymentsForAdmin(filterRequest, page, size)
        );
    }

    // 3. [ADMIN/STAFF Only] Xem chi tiết một giao dịch Payment duy nhất qua ID hóa đơn
    @GetMapping("/api/admin/payments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<PaymentResponse> getPaymentDetail(@PathVariable Long id) {
        return ApiResponse.success(
                "Lấy thông tin chi tiết giao dịch thành công",
                paymentService.getPaymentDetail(id)
        );
    }
}