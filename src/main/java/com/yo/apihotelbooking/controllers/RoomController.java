package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.CreateRoomRequest;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.services.RoomService;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    @GetMapping("/api/rooms")
    public ApiResponse<List<RoomResponse>> getAll() {
        return ApiResponse.success(roomService.getAll());
    }
    @GetMapping("/api/rooms/available")
    public ApiResponse<List<RoomResponse>> getAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ApiResponse.success(roomService.getAvailable(checkIn, checkOut));
    }

    @GetMapping("/api/rooms/{id}")
    public ApiResponse<RoomResponse> getById(@PathVariable Long id) throws NotFoundException {
        return roomService.findById(id)
                .map(ApiResponse::success)
                .orElseThrow(() -> new NotFoundException("Room not found: " + id));
    }


    @PostMapping("/api/admin/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoomResponse> create(
            @Valid @RequestBody CreateRoomRequest req) throws NotFoundException {
        return ApiResponse.success("Created", roomService.create(req));
    }

    @PutMapping("/api/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoomResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomRequest req) throws NotFoundException {
        return ApiResponse.success("Updated", roomService.update(id, req));
    }

    @DeleteMapping("/api/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) throws NotFoundException {
        roomService.delete(id);
        return ApiResponse.success("Deleted", null);
    }
}