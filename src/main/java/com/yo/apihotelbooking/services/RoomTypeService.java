package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.dto.request.CreateRoomTypeRequest;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import java.util.List;

public interface RoomTypeService {
    List<RoomTypeResponse> getAll();
  
    RoomTypeResponse getById(Long id) throws NotFoundException;

    RoomTypeResponse create(CreateRoomTypeRequest request);

    RoomTypeResponse update(Long id, CreateRoomTypeRequest request) throws NotFoundException;

    void delete(Long id) throws NotFoundException;
}