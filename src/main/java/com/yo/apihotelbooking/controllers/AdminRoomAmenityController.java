package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomAmenityRequest;
import com.yo.apihotelbooking.dto.response.RoomAmenityResponse;
import com.yo.apihotelbooking.services.RoomAmenityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/room-amenities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomAmenityController {

    private final RoomAmenityService roomAmenityService;
    
    @GetMapping("/room-type/{roomTypeId}")
    public ResponseEntity<List<RoomAmenityResponse>> getByRoomType(@PathVariable Long roomTypeId) {
        return ResponseEntity.ok(roomAmenityService.getAmenitiesByRoomType(roomTypeId));
    }

    @PostMapping
    public ResponseEntity<RoomAmenityResponse> create(@Valid @RequestBody CreateRoomAmenityRequest request) throws NotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomAmenityService.createAmenity(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomAmenityResponse> update(@PathVariable Long id, @Valid @RequestBody CreateRoomAmenityRequest request) throws NotFoundException {
        return ResponseEntity.ok(roomAmenityService.updateAmenity(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws NotFoundException {
        roomAmenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}