package com.yo.apihotelbooking.controllers;

import org.springframework.web.bind.annotation.*;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.CreateRoomTypeRequest;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.services.RoomTypeService;
import com.yo.apihotelbooking.common.exception.NotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public ApiResponse<List<RoomTypeResponse>> getAll() {
        return ApiResponse.success(roomTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomTypeResponse> getById(@PathVariable Long id) throws NotFoundException {
        return ApiResponse.success(roomTypeService.getById(id));
    }

    @PostMapping
    public ApiResponse<RoomTypeResponse> create(
            @Valid @RequestBody CreateRoomTypeRequest req
    ) {
        return ApiResponse.success("Created", roomTypeService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomTypeRequest req
    ) throws NotFoundException {
        return ApiResponse.success("Updated", roomTypeService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) throws NotFoundException {
        roomTypeService.delete(id);
        return ApiResponse.success("Deleted", null);
    }
}