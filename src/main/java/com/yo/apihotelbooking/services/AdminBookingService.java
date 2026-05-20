package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.dto.request.AdminBookingFilterRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.dto.response.UserResponse;
import com.yo.apihotelbooking.dto.response.BookingStatusHistoryResponse;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.BookingSpecification;
import com.yo.apihotelbooking.repository.PaymentRepository;
import com.yo.apihotelbooking.repository.RoomRepository;
import com.yo.apihotelbooking.repository.UserRepository;
import com.yo.apihotelbooking.repository.BookingStatusHistoryRepository;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import com.yo.apihotelbooking.schemas.domain.Payment;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentMethod;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;

    
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


    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.PENDING, BookingStatus.CONFIRMED);

        booking.setStatus(BookingStatus.CONFIRMED);
        saveHistory(booking, BookingStatus.PENDING, BookingStatus.CONFIRMED,
                "Booking confirmed by staff");

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse checkIn(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);

        LocalDate today = LocalDate.now();
        if (!today.equals(booking.getCheckInDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ được check-in vào ngày " + booking.getCheckInDate()
                    + " (hôm nay: " + today + ")");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        saveHistory(booking, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN,
                "Guest checked in at front desk");

        return toResponse(bookingRepository.save(booking));
    }


    @Transactional
    public BookingResponse checkOut(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT);

        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckedOutAt(LocalDateTime.now());
        saveHistory(booking, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT,
                "Guest checked out");

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancelBooking(Long id, String reason) {
        Booking booking = findOrThrow(id);

        if (booking.getStatus() == BookingStatus.CHECKED_OUT
                || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Không thể hủy booking ở trạng thái: " + booking.getStatus());
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);

        if (oldStatus == BookingStatus.PENDING || oldStatus == BookingStatus.CONFIRMED) {
            Payment refund = new Payment();
            refund.setBooking(booking);
            refund.setPaymentType(PaymentType.REFUND);
            refund.setAmount(booking.getTotalAmount());
            refund.setPaymentMethod(PaymentMethod.SIMULATED);
            refund.setStatus(PaymentStatus.SUCCESS);
            refund.setTransactionRef("ADMIN-REFUND-" + booking.getId() + "-" + System.currentTimeMillis());
            refund.setNote("Admin cancelled. Reason: " + reason);
            refund.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(refund);
        }

        saveHistory(booking, oldStatus, BookingStatus.CANCELLED,
                "Admin cancelled. Reason: " + reason);

        return toResponse(bookingRepository.save(booking));
    }


    @Transactional
    public BookingResponse markNoShow(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CONFIRMED, BookingStatus.NO_SHOW);

        booking.setStatus(BookingStatus.NO_SHOW);
        saveHistory(booking, BookingStatus.CONFIRMED, BookingStatus.NO_SHOW,
                "Marked as no-show by staff");

        return toResponse(bookingRepository.save(booking));
    }


    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking không tồn tại: id=" + id));
    }

    private void validateTransition(Booking booking,
                                    BookingStatus required, BookingStatus next) {
        if (booking.getStatus() != required) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Booking phải ở trạng thái " + required
                    + " mới có thể chuyển sang " + next
                    + " (hiện tại: " + booking.getStatus() + ")");
        }
    }

    private void saveHistory(Booking booking, BookingStatus oldStatus,
                             BookingStatus newStatus, String note) {
        User actor = SecurityUtils.getCurrentUser();

        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);         
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private BookingResponse toResponse(Booking b) {

        List<BookingStatusHistoryResponse> histories =
                historyRepository.findByBookingIdOrderByChangedAtDesc(b.getId())
                        .stream()
                        .map(h -> {
                            BookingStatusHistoryResponse hr = new BookingStatusHistoryResponse();
                            hr.setOldStatus(h.getOldStatus());
                            hr.setNewStatus(h.getNewStatus());
                            hr.setChangedBy(h.getChangedBy() != null
                                    ? h.getChangedBy().getFullName() : null);
                            hr.setNote(h.getNote());
                            hr.setChangedAt(h.getChangedAt());
                            return hr;
                        })
                        .toList();

        BookingResponse res = new BookingResponse();
        res.setId(b.getId());
        res.setCheckInDate(b.getCheckInDate());
        res.setCheckOutDate(b.getCheckOutDate());
        res.setNumGuests(b.getNumGuests());
        res.setTotalAmount(b.getTotalAmount());
        res.setStatus(b.getStatus());
        res.setSpecialRequests(b.getSpecialRequests());
        res.setCreatedAt(b.getCreatedAt());
        if (b.getUser() != null) {
            UserResponse userRes = new UserResponse();
            userRes.setId(b.getUser().getId());
            userRes.setFullName(b.getUser().getFullName());
            userRes.setEmail(b.getUser().getEmail());
            res.setUser(userRes);
        }

        if (b.getRoom() != null) {
            RoomResponse roomRes = new RoomResponse();
            roomRes.setId(b.getRoom().getId());
            roomRes.setRoomNumber(b.getRoom().getRoomNumber());
            roomRes.setFloor(b.getRoom().getFloor());

            if (b.getRoom().getRoomType() != null) {
                RoomTypeResponse roomTypeRes = new RoomTypeResponse();
                roomTypeRes.setId(b.getRoom().getRoomType().getId());
                roomTypeRes.setName(b.getRoom().getRoomType().getName());
                roomRes.setRoomType(roomTypeRes);
            }
            res.setRoom(roomRes);
        }

        return res;
    }
}