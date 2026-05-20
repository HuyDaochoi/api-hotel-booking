package com.yo.apihotelbooking.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.yo.apihotelbooking.schemas.domain.Room;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import com.yo.apihotelbooking.services.RoomService;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.dto.request.CreateRoomRequest;
import com.yo.apihotelbooking.dto.response.RoomAmenityResponse;
import com.yo.apihotelbooking.dto.response.RoomImageResponse;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.repository.RoomAmenityRepository;
import com.yo.apihotelbooking.repository.RoomImageRepository;
import com.yo.apihotelbooking.repository.RoomRepository;
import com.yo.apihotelbooking.repository.RoomTypeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
   
    private final RoomAmenityRepository roomAmenityRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

private RoomResponse map(Room room) {
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

        // --- Đổ dữ liệu Images từ RoomType sang DTO ---
        if (rt.getImages() != null) {
            type.setImages(rt.getImages().stream().map(img -> {
                RoomImageResponse imgDto = new RoomImageResponse();
                imgDto.setId(img.getId());
                imgDto.setImageUrl(img.getImageUrl());
                imgDto.setCaption(img.getCaption());
                imgDto.setIsPrimary(img.getIsPrimary());
                return imgDto;
            }).toList());
        }

        // --- Đổ dữ liệu Amenities từ RoomType sang DTO ---
        if (rt.getAmenities() != null) {
            type.setAmenities(rt.getAmenities().stream().map(amn -> {
                RoomAmenityResponse amnDto = new RoomAmenityResponse();
                amnDto.setId(amn.getId());
                amnDto.setAmenityName(amn.getAmenityName());
                amnDto.setIconCode(amn.getIconCode());
                return amnDto;
            }).toList());
        }

        res.setRoomType(type);
    }

    return res;
}
@Transactional(readOnly = true)
public List<RoomResponse> getAllRoomsWithDetails() {
    List<Room> rooms = roomRepository.findAllWithDetails();
    
    return rooms.stream().map(room -> {
        RoomResponse r = new RoomResponse();
        r.setId(room.getId());
        r.setRoomNumber(room.getRoomNumber());
        r.setFloor(room.getFloor());
        
        if (room.getRoomType() != null) {
            var rtEntity = room.getRoomType();
            RoomTypeResponse rtDto = new RoomTypeResponse();
            rtDto.setId(rtEntity.getId());
            rtDto.setName(rtEntity.getName());
            rtDto.setDescription(rtEntity.getDescription());
            rtDto.setBasePrice(rtEntity.getBasePrice());
            rtDto.setMaxCapacity(rtEntity.getMaxCapacity());
            
            List<RoomImageResponse> imgs = roomImageRepository.findByRoomTypeId(rtEntity.getId())
                    .stream().map(img -> {
                        RoomImageResponse imgDto = new RoomImageResponse();
                        imgDto.setId(img.getId());
                        imgDto.setImageUrl(img.getImageUrl());
                        imgDto.setCaption(img.getCaption());
                        imgDto.setIsPrimary(img.getIsPrimary());
                        return imgDto;
                    }).toList();
                    
            List<RoomAmenityResponse> amns = roomAmenityRepository.findByRoomTypeId(rtEntity.getId())
                    .stream().map(amn -> {
                        RoomAmenityResponse amnDto = new RoomAmenityResponse();
                        amnDto.setId(amn.getId());
                        amnDto.setAmenityName(amn.getAmenityName());
                        amnDto.setIconCode(amn.getIconCode());
                        return amnDto;
                    }).toList();
            
            rtDto.setImages(imgs);
            rtDto.setAmenities(amns);
            r.setRoomType(rtDto);
        }
        return r;
    }).toList();
}
@Transactional(readOnly = true)
public Optional<RoomResponse> findById(Long id) {
        return roomRepository.findById(id).map(this::map);
    }
@Transactional(readOnly = true)
    public List<RoomResponse> getAvailable(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(checkIn, checkOut)
                .stream()
                .map(this::map)
                .toList();
    }
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
@Transactional(readOnly = false)
    public void delete(Long id) throws NotFoundException {
        if (!roomRepository.existsById(id)) {
            throw new NotFoundException("Room not found with id " + id);
        }
        roomRepository.deleteById(id);
    }
}