package com.yo.apihotelbooking.services.impl;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomTypeRequest;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.services.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return roomTypeRepository.findByIsActiveTrue()
                .stream().map(this::map).toList();
    }

    public RoomTypeResponse getById(Long id) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        return map(roomType);
    }

    public RoomTypeResponse create(CreateRoomTypeRequest request) {
        RoomType roomType = mapper.map(request, RoomType.class);
        roomType.setIsActive(true);
        return map(roomTypeRepository.save(roomType));
    }

    public RoomTypeResponse update(Long id, CreateRoomTypeRequest request) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        mapper.map(request, roomType);
        return map(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public void delete(Long id) throws NotFoundException {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        roomType.setIsActive(false);
        roomTypeRepository.save(roomType);
    }
}