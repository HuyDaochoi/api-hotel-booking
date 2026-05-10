package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.query.Param;
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status NOT IN (com.yo.apihotelbooking.schemas.enums.BookingStatus.CANCELLED, " +
           "                     com.yo.apihotelbooking.schemas.enums.BookingStatus.NO_SHOW) " +
           "AND (:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId, 
            @Param("checkIn") LocalDate checkIn, 
            @Param("checkOut") LocalDate checkOut);
}