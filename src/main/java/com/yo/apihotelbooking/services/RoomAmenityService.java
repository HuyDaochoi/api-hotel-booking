package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomAmenityRequest;
import com.yo.apihotelbooking.dto.response.RoomAmenityResponse;
import java.util.List;

public interface RoomAmenityService {
    List<RoomAmenityResponse> getAmenitiesByRoomType(Long roomTypeId);
    RoomAmenityResponse createAmenity(CreateRoomAmenityRequest request) throws NotFoundException;
    RoomAmenityResponse updateAmenity(Long id, CreateRoomAmenityRequest request) throws NotFoundException; // Dùng chung CreateDTO vì các trường giống nhau
    void deleteAmenity(Long id) throws NotFoundException;
}