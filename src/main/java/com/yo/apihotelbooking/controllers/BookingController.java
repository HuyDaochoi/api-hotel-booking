package com.yo.apihotelbooking.controllers;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.services.BookingService;
import com.yo.apihotelbooking.common.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.domain.Booking;


import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;


import com.yo.apihotelbooking.dto.request.UpdateBookingStatusRequest;
import org.springframework.data.domain.Page;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

@PostMapping
public ApiResponse<?> create(@RequestBody @Valid BookingRequest request) throws Exception {
    User user = SecurityUtils.getCurrentUser();
    
    if (user == null) {
        return ApiResponse.error("Bạn cần đăng nhập để đặt phòng");
    }

    BookingResponse response = bookingService.createBooking(request, user.getId());
    return ApiResponse.success("Đặt phòng thành công", response);
}
@PatchMapping("/{id}/status")
public ApiResponse<?> updateStatus(
        @PathVariable Long id, 
        
        @RequestBody UpdateBookingStatusRequest request) throws NotFoundException {
    User admin = SecurityUtils.getCurrentUser(); 
    bookingService.updateBookingStatus(id, request.getStatus(), admin, request.getNote());
    return ApiResponse.success("Cập nhật trạng thái thành công", null);
}

@GetMapping("/my")
    public ResponseEntity<Page<Booking>> getMyBookings(
            @AuthenticationPrincipal User currentUser, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser, page, size));
    }
@GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBookingDetail(@PathVariable Long id) {
        Booking booking = bookingService.getBookingDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thành công", booking));
    }
@DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Hủy đơn đặt phòng thành công!"));
    }


}