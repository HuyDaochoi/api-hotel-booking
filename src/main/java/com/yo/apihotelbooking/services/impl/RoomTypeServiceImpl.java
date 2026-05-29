package com.yo.apihotelbooking.services.impl;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomTypeRequest;
import com.yo.apihotelbooking.dto.response.AmenitiesResponse;
import com.yo.apihotelbooking.dto.response.RoomImageResponse;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.repository.AmenitiesRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.schemas.domain.RoomImage;
import com.yo.apihotelbooking.schemas.domain.Amenities;
import com.yo.apihotelbooking.services.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final AmenitiesRepository AmenitiesRepository;

    private RoomTypeResponse map(RoomType roomType) {
        if (roomType == null) return null;
        
        RoomTypeResponse res = new RoomTypeResponse();
        res.setId(roomType.getId());
        res.setName(roomType.getName());
        res.setDescription(roomType.getDescription());
        res.setBasePrice(roomType.getBasePrice());
        res.setMaxCapacity(roomType.getMaxCapacity());
        res.setCancellationPolicy(roomType.getCancellationPolicy());
        res.setIsActive(roomType.getIsActive());
        
        if (roomType.getImages() != null) {
            res.setImages(roomType.getImages().stream().map(img -> {
                RoomImageResponse imgDto = new RoomImageResponse();
                imgDto.setId(img.getId());
                imgDto.setImageUrl(img.getImageUrl());
                imgDto.setCaption(img.getCaption());
                imgDto.setIsPrimary(img.getIsPrimary());
                return imgDto;
            }).toList());
        }
        
        if (roomType.getAmenities() != null) {
            res.setAmenities(roomType.getAmenities().stream().map(amn -> {
                AmenitiesResponse amnDto = new AmenitiesResponse();
                amnDto.setId(amn.getId());
                amnDto.setAmenityName(amn.getName());
                amnDto.setIconCode(amn.getIconCode());
                return amnDto;
            }).toList());
        }
        
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getAll() {
        return roomTypeRepository.findByIsActiveTrue()
                .stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getById(Long id) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        return map(roomType);
    }

    @Override
    @Transactional(readOnly = false)
    public RoomTypeResponse create(CreateRoomTypeRequest request) {
        RoomType roomType = new RoomType();
        roomType.setName(request.getName());
        roomType.setDescription(request.getDescription());
        roomType.setBasePrice(request.getBasePrice());
        roomType.setMaxCapacity(request.getMaxCapacity());
        roomType.setCancellationPolicy(request.getCancellationPolicy());
        roomType.setIsActive(true);

        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            List<Amenities> existingAmenities = AmenitiesRepository.findAllById(request.getAmenityIds());
            roomType.setAmenities(existingAmenities);
        }

        if (request.getImages() != null) {
            List<RoomImage> images = new ArrayList<>();
            request.getImages().forEach(imgReq -> {
                RoomImage img = new RoomImage();
                img.setImageUrl(imgReq.getImageUrl());
                img.setCaption(imgReq.getCaption());
                img.setIsPrimary(imgReq.getIsPrimary() != null ? imgReq.getIsPrimary() : false);
                img.setSortOrder(imgReq.getSortOrder() != null ? imgReq.getSortOrder() : 0);
                img.setRoomType(roomType);
                images.add(img);
            });
            roomType.setImages(images);
        }

        RoomType saved = roomTypeRepository.save(roomType);
        return map(saved);
    }

    @Override
    @Transactional(readOnly = false)
    public RoomTypeResponse update(Long id, CreateRoomTypeRequest request) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));

        roomType.setName(request.getName());
        roomType.setDescription(request.getDescription());
        roomType.setBasePrice(request.getBasePrice());
        roomType.setMaxCapacity(request.getMaxCapacity());
        roomType.setCancellationPolicy(request.getCancellationPolicy());

        if (roomType.getImages() != null) {
            roomType.getImages().clear();
            if (request.getImages() != null) {
                request.getImages().forEach(imgReq -> {
                    RoomImage img = new RoomImage();
                    img.setImageUrl(imgReq.getImageUrl());
                    img.setCaption(imgReq.getCaption());
                    img.setIsPrimary(imgReq.getIsPrimary() != null ? imgReq.getIsPrimary() : false);
                    img.setSortOrder(imgReq.getSortOrder() != null ? imgReq.getSortOrder() : 0);
                    img.setRoomType(roomType);
                    roomType.getImages().add(img);
                });
            }
        }

        if (roomType.getAmenities() != null) {
            roomType.getAmenities().clear();
            if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
                List<Amenities> existingAmenities = AmenitiesRepository.findAllById(request.getAmenityIds());
                roomType.getAmenities().addAll(existingAmenities);
            }
        }

        return map(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Long id) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        
        roomType.setIsActive(false);
        roomTypeRepository.save(roomType);
    }
}