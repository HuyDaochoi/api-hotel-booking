package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomImageRequest;
import com.yo.apihotelbooking.dto.request.UpdateRoomImageRequest;
import com.yo.apihotelbooking.dto.response.RoomImageResponse;
import java.util.List;

public interface RoomImageService {
    List<RoomImageResponse> getImagesByRoomType(Long roomTypeId);
    RoomImageResponse createImage(CreateRoomImageRequest request) throws NotFoundException;
    RoomImageResponse updateImage(Long id, UpdateRoomImageRequest request) throws NotFoundException;
    void deleteImage(Long id) throws NotFoundException;
}