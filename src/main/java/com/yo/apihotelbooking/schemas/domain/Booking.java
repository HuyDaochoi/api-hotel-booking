package com.yo.apihotelbooking.schemas.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.List;
@Entity
@Data
@Table(name = "bookings")
public class Booking extends AuditableEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

     @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "num_guests")
    private Integer numGuests;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(columnDefinition = "text")
    private String specialRequests;

    @Column(columnDefinition = "text")
    private String cancellationReason;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;
    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @OneToMany(mappedBy = "booking")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Payment> payments;

}
