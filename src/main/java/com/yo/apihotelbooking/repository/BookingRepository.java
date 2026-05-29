package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.Booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface BookingRepository extends JpaRepository<Booking, Long> ,
JpaSpecificationExecutor<Booking> {

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status NOT IN (com.yo.apihotelbooking.schemas.enums.BookingStatus.CANCELLED, " +
            "                     com.yo.apihotelbooking.schemas.enums.BookingStatus.NO_SHOW, " +
            "                     com.yo.apihotelbooking.schemas.enums.BookingStatus.CHECKED_OUT) " +
            "AND (:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.room r " +
            "LEFT JOIN FETCH r.roomType " +
            "LEFT JOIN FETCH b.payments " +
            "WHERE b.id = :id")
    Optional<Booking> findDetailById(@Param("id") Long id);

}
