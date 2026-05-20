package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);
  @Query("SELECT r FROM Room r LEFT JOIN FETCH r.roomType")
List<Room> findAllWithDetails();

    @Query(value = """
        SELECT r.* FROM rooms r
        WHERE r.is_active = 1
          AND r.id NOT IN (
              SELECT b.room_id FROM bookings b
              WHERE b.status NOT IN ('CANCELLED','NO_SHOW')
                AND :checkIn < b.check_out_date
                AND :checkOut > b.check_in_date
          )
    """, nativeQuery = true)
    List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut);
}
