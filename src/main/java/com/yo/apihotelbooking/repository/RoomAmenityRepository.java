package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.RoomAmenities;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenities, Long> {

    List<RoomAmenities> findByRoomTypeId(Long roomTypeId);

}