package com.yo.apihotelbooking.services.impl;

import org.springframework.stereotype.Service;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import lombok.RequiredArgsConstructor;

import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.services.RoomTypeService;

import org.modelmapper.ModelMapper;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.dto.request.CreateRoomTypeRequest;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final ModelMapper mapper;

      private RoomTypeResponse map(RoomType roomType) {
        return mapper.map(roomType, RoomTypeResponse.class);
    }
    public List<RoomTypeResponse> getAll() {
        List<RoomType> roomTypes = roomTypeRepository.findByIsActiveTrue();
        return roomTypes.stream().map(this::map).toList();
    }

    public RoomTypeResponse getById(Long id) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        return map(roomType);
    }
    public RoomTypeResponse create(CreateRoomTypeRequest request) {
        RoomType roomType = mapper.map(request, RoomType.class);
        roomType.setIsActive(true);
        RoomType saved = roomTypeRepository.save(roomType);
        return map(saved);
    }

    public RoomTypeResponse update(Long id, CreateRoomTypeRequest request) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        mapper.map(request, roomType);
        RoomType updated = roomTypeRepository.save(roomType);
        return map(updated);
    }

    public void delete(Long id) throws NotFoundException {
        if (!roomTypeRepository.existsById(id)) {
            throw new NotFoundException("Room type not found with id: " + id);
        }
        roomTypeRepository.deleteById(id);
    }
}
