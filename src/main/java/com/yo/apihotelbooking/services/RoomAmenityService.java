package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomAmenityRequest;
import com.yo.apihotelbooking.dto.response.AmenitiesResponse;
import java.util.List;

public interface RoomAmenityService {
    List<AmenitiesResponse> getAmenitiesByRoomType(Long roomTypeId);
    AmenitiesResponse createAmenity(CreateRoomAmenityRequest request) throws NotFoundException;
    AmenitiesResponse updateAmenity(Long id, CreateRoomAmenityRequest request) throws NotFoundException; // Dùng chung CreateDTO vì các trường giống nhau
    void deleteAmenity(Long id) throws NotFoundException;
}