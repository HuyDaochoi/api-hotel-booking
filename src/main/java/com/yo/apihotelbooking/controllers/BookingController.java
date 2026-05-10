package com.yo.apihotelbooking.controllers;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.services.BookingService;
import com.yo.apihotelbooking.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.repository.UserRepository;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.dto.request.UpdateBookingStatusRequest;
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
}