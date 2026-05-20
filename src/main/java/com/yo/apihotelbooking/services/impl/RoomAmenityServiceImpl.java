package com.yo.apihotelbooking.services.impl;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomAmenityRequest;
import com.yo.apihotelbooking.dto.response.RoomAmenityResponse;
import com.yo.apihotelbooking.repository.RoomAmenityRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.schemas.domain.RoomAmenity;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.services.RoomAmenityService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAmenityServiceImpl implements RoomAmenityService {

    private final RoomAmenityRepository roomAmenityRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ModelMapper mapper;

    @Override
    public List<RoomAmenityResponse> getAmenitiesByRoomType(Long roomTypeId) {
        return roomAmenityRepository.findByRoomTypeId(roomTypeId).stream()
                .map(amn -> mapper.map(amn, RoomAmenityResponse.class)).toList();
    }

    @Override
    @Transactional
    public RoomAmenityResponse createAmenity(CreateRoomAmenityRequest req) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(req.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException("RoomType not found with id: " + req.getRoomTypeId()));

        RoomAmenity amenity = mapper.map(req, RoomAmenity.class);
        amenity.setRoomType(roomType);

        return mapper.map(roomAmenityRepository.save(amenity), RoomAmenityResponse.class);
    }

    @Override
    @Transactional
    public RoomAmenityResponse updateAmenity(Long id, CreateRoomAmenityRequest req) throws NotFoundException {
        RoomAmenity amenity = roomAmenityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Amenity not found with id: " + id));

        amenity.setAmenityName(req.getAmenityName());
        amenity.setIconCode(req.getIconCode());

        return mapper.map(roomAmenityRepository.save(amenity), RoomAmenityResponse.class);
    }

    @Override
    @Transactional
    public void deleteAmenity(Long id) throws NotFoundException {
        if (!roomAmenityRepository.existsById(id)) {
            throw new NotFoundException("Amenity not found with id: " + id);
        }
        roomAmenityRepository.deleteById(id);
    }
}