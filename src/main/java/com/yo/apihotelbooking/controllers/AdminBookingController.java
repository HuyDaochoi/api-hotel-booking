package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.dto.request.AdminBookingFilterRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.services.AdminBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAllBookings(@Valid AdminBookingFilterRequest filter) {
        Page<BookingResponse> response = adminBookingService.getAllBookings(filter);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        BookingResponse response = adminBookingService.confirmBooking(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/check-in")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long id) {
        BookingResponse response = adminBookingService.checkIn(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/check-out")
    public ResponseEntity<BookingResponse> checkOut(@PathVariable Long id) {
        BookingResponse response = adminBookingService.checkOut(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Admin hủy đơn chủ động") String reason,
            @RequestParam(required = false) java.math.BigDecimal refundPercent) {
        BookingResponse response = adminBookingService.cancelBooking(id, reason, refundPercent);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/change-room")
    public ResponseEntity<BookingResponse> changeRoom(
            @PathVariable Long id,
            @RequestParam Long newRoomId,
            @RequestParam(required = false, defaultValue = "Khách đồng ý đổi phòng do sự cố") String reason) {
        BookingResponse response = adminBookingService.changeRoom(id, newRoomId, reason);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable Long id) {
        BookingResponse response = adminBookingService.markNoShow(id);
        return ResponseEntity.ok(response);
    }
}