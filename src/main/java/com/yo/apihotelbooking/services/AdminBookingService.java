package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.dto.request.AdminBookingFilterRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.BookingSpecification;
import com.yo.apihotelbooking.repository.BookingStatusHistoryRepository;
import com.yo.apihotelbooking.repository.PaymentRepository;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import com.yo.apihotelbooking.schemas.domain.Payment;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.yo.apihotelbooking.schemas.domain.CancellationPolicy;
import com.yo.apihotelbooking.repository.CancellationPolicyRepository;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final CancellationPolicyRepository cancellationPolicyRepository;

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(AdminBookingFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Booking> spec = BookingSpecification.filterBy(
                filter.getStatus(),
                filter.getCheckIn(),
                filter.getCheckOut(),
                filter.getRoomId(),
                filter.getUserId()
        );

        return bookingRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse confirmBooking(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.PENDING, BookingStatus.CONFIRMED);

        User actor = SecurityUtils.getCurrentUser();
        bookingService.updateBookingStatus(id, BookingStatus.CONFIRMED, actor, "Booking confirmed by staff");

        entityManager.clear();
        return toResponse(findOrThrow(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse checkIn(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);

        LocalDate today = LocalDate.now();
        if (!today.equals(booking.getCheckInDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ được check-in vào ngày " + booking.getCheckInDate()
                            + " (hôm nay: " + today + ")");
        }

        User actor = SecurityUtils.getCurrentUser();
        bookingService.updateBookingStatus(id, BookingStatus.CHECKED_IN, actor, "Guest checked in at front desk");

        entityManager.clear();
        return toResponse(findOrThrow(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse checkOut(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT);

        User actor = SecurityUtils.getCurrentUser();
        bookingService.updateBookingStatus(id, BookingStatus.CHECKED_OUT, actor, "Guest checked out");

        entityManager.clear();
        return toResponse(findOrThrow(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse cancelBooking(Long id, String reason) {
        Booking booking = findOrThrow(id);
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);

        LocalDateTime checkInDateTime = booking.getCheckInDate().atTime(14, 0);
        long hoursUntilCheckIn = java.time.temporal.ChronoUnit.HOURS.between(LocalDateTime.now(), checkInDateTime);

        BigDecimal refundPercent = bookingService.getRefundPercentByPolicy(booking.getRoom().getRoomType().getId(), hoursUntilCheckIn);

        List<Payment> payments = paymentRepository.findByBookingId(id);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && p.getPaymentType() == PaymentType.PAYMENT)
                .map(p -> p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal refundAmount = totalPaid.multiply(refundPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            Payment refund = new Payment();
            refund.setBooking(booking);
            refund.setPaymentType(PaymentType.REFUND);
            refund.setAmount(refundAmount);
            refund.setAmountPaid(refundAmount);
            refund.setPaymentMethod(PaymentMethod.SIMULATED);
            refund.setStatus(PaymentStatus.SUCCESS);
            refund.setTransactionRef("ADMIN-RF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            refund.setNote("Admin hủy đơn. Hoàn trả " + refundPercent + "%. Lý do: " + reason);
            refund.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(refund);
        }

        bookingRepository.saveAndFlush(booking);

        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setOldStatus(oldStatus);
        history.setNewStatus(BookingStatus.CANCELLED);
        history.setChangedBy(SecurityUtils.getCurrentUser());
        history.setNote("Admin hủy đơn. Hoàn trả " + refundPercent + "%: " + refundAmount + " VNĐ. Lý do: " + reason);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        bookingService.recalculatePaymentStatus(id);

        return toResponse(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse markNoShow(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CONFIRMED, BookingStatus.NO_SHOW);

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.NO_SHOW);

        bookingRepository.saveAndFlush(booking);

        User actor = SecurityUtils.getCurrentUser();
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setOldStatus(oldStatus);
        history.setNewStatus(BookingStatus.NO_SHOW);
        history.setChangedBy(actor);
        history.setNote("Nhân viên đánh dấu khách không đến (No-Show). Phạt giữ toàn bộ tiền cọc.");
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        bookingService.recalculatePaymentStatus(id);
        entityManager.refresh(booking);

        return toResponse(booking);
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking không tồn tại: id=" + id));
    }

    private void validateTransition(Booking booking, BookingStatus required, BookingStatus next) {
        if (booking.getStatus() != required) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking phải ở trạng thái " + required + " mới có thể chuyển sang " + next + " (hiện tại: " + booking.getStatus() + ")");
        }
    }

    private BookingResponse toResponse(Booking b) {
        return bookingService.toResponse(b);
    }
}