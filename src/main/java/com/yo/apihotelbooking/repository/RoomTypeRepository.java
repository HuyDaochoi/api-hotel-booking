package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByIsActiveTrue();
@Query("SELECT DISTINCT rt FROM RoomType rt " +
       "JOIN rt.amenities a " +
       "WHERE (:guestCount IS NULL OR rt.maxCapacity >= :guestCount) " +
       "AND (:amenityIds IS NULL OR a.id IN :amenityIds)")
List<RoomType> searchRoomTypes(
        @Param("guestCount") Integer guestCount,
        @Param("amenityIds") List<Long> amenityIds
);
}