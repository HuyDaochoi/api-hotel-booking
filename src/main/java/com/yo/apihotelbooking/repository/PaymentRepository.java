package com.yo.apihotelbooking.repository;

import com.yo.apihotelbooking.schemas.domain.Payment;
import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p WHERE " +
           "(:bookingId IS NULL OR p.booking.id = :bookingId) AND " +
           "(:paymentType IS NULL OR p.paymentType = :paymentType) AND " +
           "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:start IS NULL OR p.processedAt >= :start) AND " +
           "(:end IS NULL OR p.processedAt <= :end)")
    Page<Payment> findAllWithFilter(
            @Param("bookingId") Long bookingId,
            @Param("paymentType") PaymentType paymentType,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}