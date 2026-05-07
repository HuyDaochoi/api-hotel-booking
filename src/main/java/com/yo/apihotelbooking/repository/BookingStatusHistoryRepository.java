package com.yo.apihotelbooking.repository;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtDesc(Long bookingId);

}