package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.dto.request.AdminBookingFilterRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.services.AdminBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Page<BookingResponse>> getAllBookings(
            AdminBookingFilterRequest filter   // @ModelAttribute ẩn — Spring tự bind query params
    ) {
        return ResponseEntity.ok(adminBookingService.getAllBookings(filter));
    }


    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(adminBookingService.confirmBooking(id));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/admin/bookings/{id}/check-in
    // CONFIRMED → CHECKED_IN | Chỉ đúng ngày checkInDate | Role: STAFF, ADMIN
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BookingResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(adminBookingService.checkIn(id));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/admin/bookings/{id}/check-out
    // CHECKED_IN → CHECKED_OUT | Role: STAFF, ADMIN
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{id}/check-out")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BookingResponse> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(adminBookingService.checkOut(id));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/admin/bookings/{id}/cancel
    // Hủy bất kỳ trạng thái (trừ CHECKED_OUT/CANCELLED) | Role: ADMIN
    // Body: { "reason": "..." }
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String reason = body.getOrDefault("reason", "Cancelled by admin");
        return ResponseEntity.ok(adminBookingService.cancelBooking(id, reason));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/admin/bookings/{id}/no-show
    // CONFIRMED → NO_SHOW | Role: STAFF, ADMIN
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<BookingResponse> markNoShow(@PathVariable Long id) {
        return ResponseEntity.ok(adminBookingService.markNoShow(id));
    }
}