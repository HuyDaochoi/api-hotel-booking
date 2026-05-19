package com.yo.apihotelbooking.services.impl;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomImageRequest;
import com.yo.apihotelbooking.dto.request.UpdateRoomImageRequest;
import com.yo.apihotelbooking.dto.response.RoomImageResponse;
import com.yo.apihotelbooking.repository.RoomImageRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.schemas.domain.RoomImage;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.services.RoomImageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomImageServiceImpl implements RoomImageService {

    private final RoomImageRepository roomImageRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ModelMapper mapper;

    @Override
    public List<RoomImageResponse> getImagesByRoomType(Long roomTypeId) {
        return roomImageRepository.findByRoomTypeId(roomTypeId).stream()
                .map(img -> mapper.map(img, RoomImageResponse.class)).toList();
    }

    @Override
    @Transactional
    public RoomImageResponse createImage(CreateRoomImageRequest req) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(req.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException("RoomType not found with id: " + req.getRoomTypeId()));

        RoomImage roomImage = mapper.map(req, RoomImage.class);
        roomImage.setRoomType(roomType); // Gán object cha để giữ khóa ngoại

        return mapper.map(roomImageRepository.save(roomImage), RoomImageResponse.class);
    }

    @Override
    @Transactional
    public RoomImageResponse updateImage(Long id, UpdateRoomImageRequest req) throws NotFoundException {
        RoomImage roomImage = roomImageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));

        // Cập nhật các thông tin thay đổi
        roomImage.setImageUrl(req.getImageUrl());
        roomImage.setCaption(req.getCaption());
        if (req.getIsPrimary() != null) roomImage.setIsPrimary(req.getIsPrimary());
        if (req.getSortOrder() != null) roomImage.setSortOrder(req.getSortOrder());

        return mapper.map(roomImageRepository.save(roomImage), RoomImageResponse.class);
    }

    @Override
    @Transactional
    public void deleteImage(Long id) throws NotFoundException {
        if (!roomImageRepository.existsById(id)) {
            throw new NotFoundException("Image not found with id: " + id);
        }
        roomImageRepository.deleteById(id);
    }
}