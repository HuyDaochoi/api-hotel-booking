package com.yo.apihotelbooking.schemas.domain;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
@Entity
@Table(name = "booking_status_history")
@Getter @Setter
public class BookingStatusHistory extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private BookingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private BookingStatus newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy;
    @Column(columnDefinition = "text")
    private String note;

    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();;
}
