package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomImageRequest;
import com.yo.apihotelbooking.dto.request.UpdateRoomImageRequest;
import com.yo.apihotelbooking.dto.response.RoomImageResponse;
import com.yo.apihotelbooking.services.RoomImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/room-images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomImageController {

    private final RoomImageService roomImageService;

    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<List<RoomImageResponse>> getByRoomType(@PathVariable Long roomTypeId) {
        return ResponseEntity.ok(roomImageService.getImagesByRoomType(roomTypeId));
    }

    @PostMapping
    public ResponseEntity<RoomImageResponse> create(@Valid @RequestBody CreateRoomImageRequest request) throws NotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomImageService.createImage(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomImageResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateRoomImageRequest request) throws NotFoundException {
        return ResponseEntity.ok(roomImageService.updateImage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws NotFoundException {
        roomImageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}