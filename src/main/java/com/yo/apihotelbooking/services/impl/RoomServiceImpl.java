package com.yo.apihotelbooking.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.yo.apihotelbooking.schemas.domain.Room;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.services.RoomService;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomRequest;
import com.yo.apihotelbooking.dto.request.RoomSearchRequest;
import com.yo.apihotelbooking.dto.response.AmenitiesResponse;
import com.yo.apihotelbooking.dto.response.RoomImageResponse;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.repository.RoomRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    private RoomResponse map(Room room) {
        if (room == null) return null;
        
        RoomResponse res = new RoomResponse();
        res.setId(room.getId());
        res.setRoomNumber(room.getRoomNumber());
        res.setFloor(room.getFloor());

        if (room.getRoomType() != null) {
            var rt = room.getRoomType();

            RoomTypeResponse type = new RoomTypeResponse();
            type.setId(rt.getId());
            type.setName(rt.getName());
            type.setDescription(rt.getDescription());
            type.setBasePrice(rt.getBasePrice());
            type.setMaxCapacity(rt.getMaxCapacity());
            if (rt.getImages() != null) {
                List<RoomImageResponse> imgs = rt.getImages().stream().map(img -> {
                    RoomImageResponse imgDto = new RoomImageResponse();
                    imgDto.setId(img.getId());
                    imgDto.setImageUrl(img.getImageUrl());
                    imgDto.setCaption(img.getCaption());
                    imgDto.setIsPrimary(img.getIsPrimary());
                    return imgDto;
                }).toList();
                type.setImages(imgs);
            }
            if (rt.getAmenities() != null) {
                List<AmenitiesResponse> amns = rt.getAmenities().stream().map(amn -> {
                    AmenitiesResponse amnDto = new AmenitiesResponse();
                    amnDto.setId(amn.getId());
                    amnDto.setAmenityName(amn.getName());
                    amnDto.setIconCode(amn.getIconCode());
                    return amnDto;
                }).toList();
                type.setAmenities(amns);
            }

            res.setRoomType(type);
        }

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRoomsWithDetails() {
        return roomRepository.findAllWithDetails()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomResponse> findById(Long id) {
        return roomRepository.findById(id).map(this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailable(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(checkIn, checkOut)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = false)
    public RoomResponse create(CreateRoomRequest req) throws NotFoundException {
        if (roomRepository.findByRoomNumber(req.getRoomNumber()).isPresent()) {
            throw new RuntimeException("Room number already exists");
        }

        Room room = new Room();
        room.setRoomNumber(req.getRoomNumber());
        room.setFloor(req.getFloor());
        room.setDescription(req.getDescription());

        room.setRoomType(
                roomTypeRepository.findById(req.getRoomTypeId())
                        .orElseThrow(() -> new NotFoundException("RoomType not found"))
        );

        Room saved = roomRepository.save(room);
        return map(saved);
    }

    @Override
    @Transactional(readOnly = false)
    public RoomResponse update(Long id, CreateRoomRequest req) throws NotFoundException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        room.setRoomNumber(req.getRoomNumber());
        room.setFloor(req.getFloor());
        room.setDescription(req.getDescription());

        room.setRoomType(
                roomTypeRepository.findById(req.getRoomTypeId())
                        .orElseThrow(() -> new NotFoundException("RoomType not found"))
        );

        Room updated = roomRepository.save(room);
        return map(updated);
    }

@Override
    @Transactional(readOnly = true)
    public List<RoomResponse> searchRooms(RoomSearchRequest req) {
        List<RoomType> matchedTypes = roomTypeRepository.searchRoomTypes(
                req.getGuestCount(), 
                req.getAmenityIds()
        );
        if (matchedTypes.isEmpty()) return List.of();

        List<Long> matchedTypeIds = matchedTypes.stream().map(RoomType::getId).toList();
        List<Room> rooms = (req.getCheckIn() != null && req.getCheckOut() != null)
                ? roomRepository.findAvailableRooms(req.getCheckIn(), req.getCheckOut())
                : roomRepository.findAll();
        return rooms.stream()
                .filter(room -> room.getRoomType() != null && matchedTypeIds.contains(room.getRoomType().getId()))
                .map(this::map)
                .toList();
    }
    @Override
    @Transactional(readOnly = false)
    public void delete(Long id) throws NotFoundException {
        if (!roomRepository.existsById(id)) {
            throw new NotFoundException("Room not found with id " + id);
        }
        roomRepository.deleteById(id);
    }

}