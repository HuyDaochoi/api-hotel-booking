package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.RoomImages;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RoomImageRepository extends JpaRepository<RoomImages, Long> {

    List<RoomImages> findByRoomTypeId(Long roomTypeId);

}
