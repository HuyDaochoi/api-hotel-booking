package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.dto.request.PaymentFilterRequest;
import com.yo.apihotelbooking.dto.response.PaymentResponse;
import com.yo.apihotelbooking.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @ModelAttribute PaymentFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentService.getAllPaymentsForAdmin(filter, page, size));
    }

    @PutMapping("/{id}/success")
    public ResponseEntity<PaymentResponse> markAsSuccess(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionRef,
            @RequestParam(required = false, defaultValue = "Admin xác nhận thanh toán thủ công") String note) {
        return ResponseEntity.ok(paymentService.processPaymentSuccess(id, transactionRef, note));
    }

    @PutMapping("/{id}/failed")
    public ResponseEntity<PaymentResponse> markAsFailed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Sai thông tin chuyển khoản hoặc chưa nhận được tiền") String reason) throws BadRequestException {
        return ResponseEntity.ok(paymentService.processPaymentFailed(id, reason));
    }
}