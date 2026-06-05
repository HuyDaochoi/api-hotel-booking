package com.yo.apihotelbooking.services.impl;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomAmenityRequest;
import com.yo.apihotelbooking.dto.response.AmenitiesResponse;
import com.yo.apihotelbooking.repository.AmenitiesRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.schemas.domain.Amenities;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.services.RoomAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAmenityServiceImpl implements RoomAmenityService {

    private final AmenitiesRepository roomAmenityRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AmenitiesResponse> getAmenitiesByRoomType(Long roomTypeId) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("RoomType not found with id: " + roomTypeId));

        // Dùng Amenities thay vì RoomAmenity
        return roomType.getAmenities().stream().map(amn -> {
            AmenitiesResponse res = new AmenitiesResponse();
            res.setId(amn.getId());
            res.setAmenityName(amn.getName());     // Sửa từ getAmenityName() thành getName()
            res.setIconCode(amn.getIconCode());
            return res;
        }).toList();
    }

    @Override
    @Transactional(readOnly = false)
    public AmenitiesResponse createAmenity(CreateRoomAmenityRequest req) {
        Amenities amenity = new Amenities();       // Sửa thành Amenities
        amenity.setName(req.getAmenityName());     // Sửa từ setAmenityName() thành setName()
        amenity.setIconCode(req.getIconCode());

        Amenities saved = roomAmenityRepository.save(amenity);

        AmenitiesResponse res = new AmenitiesResponse();
        res.setId(saved.getId());
        res.setAmenityName(saved.getName());       // Sửa từ getName()
        res.setIconCode(saved.getIconCode());
        return res;
    }

    @Override
    @Transactional(readOnly = false)
    public AmenitiesResponse updateAmenity(Long id, CreateRoomAmenityRequest req) throws NotFoundException {
        Amenities amenity = roomAmenityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Amenity not found with id: " + id));

        amenity.setName(req.getAmenityName());     // Sửa từ setName()
        amenity.setIconCode(req.getIconCode());

        Amenities saved = roomAmenityRepository.save(amenity);

        AmenitiesResponse res = new AmenitiesResponse();
        res.setId(saved.getId());
        res.setAmenityName(saved.getName());       // Sửa từ getName()
        res.setIconCode(saved.getIconCode());
        return res;
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAmenity(Long id) throws NotFoundException {
        if (!roomAmenityRepository.existsById(id)) {
            throw new NotFoundException("Amenity not found with id: " + id);
        }
        roomAmenityRepository.deleteById(id);
    }
}