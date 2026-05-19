package com.yo.apihotelbooking.controllers;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.services.BookingService;
import com.yo.apihotelbooking.common.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.yo.apihotelbooking.schemas.domain.User;



import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.util.SecurityUtils;


import com.yo.apihotelbooking.dto.request.UpdateBookingStatusRequest;
import org.springframework.data.domain.Page;
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

   @PostMapping
    public ApiResponse<BookingResponse> create(
            @RequestBody @Valid BookingRequest request) throws Exception {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            return ApiResponse.error("Bạn cần đăng nhập để đặt phòng");
        }
        return ApiResponse.success("Đặt phòng thành công",
                bookingService.createBooking(request, user.getId()));
    }
    @PatchMapping("/{id}/status")
    public ApiResponse<BookingResponse> updateStatus(
        @PathVariable Long id, 
        
        @RequestBody UpdateBookingStatusRequest request) throws NotFoundException {
    User admin = SecurityUtils.getCurrentUser(); 
    bookingService.updateBookingStatus(id, request.getStatus(), admin, request.getNote());
    return ApiResponse.success("Cập nhật trạng thái thành công", null);
    }

  @GetMapping("/my")
    public ApiResponse<Page<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(bookingService.getMyBookings(user, page, size));
    }
@GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingDetail(@PathVariable Long id) throws NotFoundException, BadRequestException {
        BookingResponse booking = bookingService.getBookingDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thành công", booking));
    }
@DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id) throws NotFoundException, BadRequestException {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Hủy đơn đặt phòng thành công!"));
    }


}