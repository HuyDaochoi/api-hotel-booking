package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.CreateRoomTypeRequest;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.services.RoomTypeService;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping("/api/room-types")
    public ApiResponse<List<RoomTypeResponse>> getAll() {
        return ApiResponse.success(roomTypeService.getAll());
    }

    @GetMapping("/api/room-types/{id}")
    public ApiResponse<RoomTypeResponse> getById(@PathVariable Long id) throws NotFoundException {
        return ApiResponse.success(roomTypeService.getById(id));
    }
    
    @PostMapping("/api/admin/room-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoomTypeResponse> create(
            @Valid @RequestBody CreateRoomTypeRequest req) {
        return ApiResponse.success("Created", roomTypeService.create(req));
    }

    @PutMapping("/api/admin/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoomTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomTypeRequest req) throws NotFoundException {
        return ApiResponse.success("Updated", roomTypeService.update(id, req));
    }

    @DeleteMapping("/api/admin/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) throws NotFoundException {
        roomTypeService.delete(id);
        return ApiResponse.success("Deleted", null);
    }
}