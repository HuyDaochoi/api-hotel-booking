package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status NOT IN ('CANCELLED','NO_SHOW')
        AND :checkIn < b.checkOutDate
        AND :checkOut > b.checkInDate
    """)
    List<Booking> checkConflict(Long roomId, LocalDate checkIn, LocalDate checkOut);
    List<Booking> findByStatus(BookingStatus status);
}