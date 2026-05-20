package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.RoomAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Long> {

    List<RoomAmenity> findByRoomTypeId(Long roomTypeId);

}