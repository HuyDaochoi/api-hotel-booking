package com.yo.apihotelbooking.services;

import java.time.LocalDate;

import com.yo.apihotelbooking.dto.request.CreateRoomRequest;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
@Service
public interface RoomService {
    
    List<RoomResponse> getAll();

    Optional<RoomResponse> findById(Long id);
    List<RoomResponse> getAvailable(LocalDate checkIn, LocalDate checkOut);
    RoomResponse create(CreateRoomRequest request) throws NotFoundException;
    RoomResponse update(Long id, CreateRoomRequest request) throws NotFoundException;
    void delete(Long id) throws NotFoundException;
}
