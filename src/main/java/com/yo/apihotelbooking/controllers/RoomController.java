package com.yo.apihotelbooking.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.CreateRoomRequest;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.services.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
//admin + staff đều có thể xem danh sách phòng và chi tiết phòng, nhưng chỉ admin mới được tạo/sửa/xóa phòng
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import com.yo.apihotelbooking.common.exception.NotFoundException;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ApiResponse<List<RoomResponse>> getAll() {
        return ApiResponse.success(roomService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getById(@PathVariable Long id) throws NotFoundException {
        return roomService.findById(id)
                .map(ApiResponse::success)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
    }

    @GetMapping("/available")
    public ApiResponse<List<RoomResponse>> getAvailable(
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut
    ) {
        return ApiResponse.success(
                roomService.getAvailable(checkIn, checkOut)
        );
    }

    @PostMapping
    public ApiResponse<RoomResponse> create(@Valid @RequestBody CreateRoomRequest req)
            throws NotFoundException {
        return ApiResponse.success("Created", roomService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomRequest req
    ) throws NotFoundException {
        return ApiResponse.success(
                "Updated",
                roomService.update(id, req)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) throws NotFoundException {
        roomService.delete(id);
        return ApiResponse.success("Deleted", null);
    }
}