package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.dto.request.AdminBookingFilterRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.dto.response.UserResponse;
import com.yo.apihotelbooking.dto.response.BookingStatusHistoryResponse;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.BookingSpecification;
import com.yo.apihotelbooking.repository.RoomRepository;
import com.yo.apihotelbooking.repository.UserRepository;
import com.yo.apihotelbooking.repository.BookingStatusHistoryRepository;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
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

    // ─────────────────────────────────────────────────────────────
    // 1. GET /api/admin/bookings — filter + phân trang
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(AdminBookingFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // ✅ FIX: gọi BookingSpecification.filterBy() trực tiếp, không qua spec package
        Specification<Booking> spec = BookingSpecification.filterBy(
                filter.getStatus(),
                filter.getCheckIn(),
                filter.getCheckOut(),
                filter.getRoomId(),
                filter.getUserId()
        );

        return bookingRepository.findAll(spec, pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // 2. PUT /api/admin/bookings/{id}/confirm — PENDING → CONFIRMED
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.PENDING, BookingStatus.CONFIRMED);

        booking.setStatus(BookingStatus.CONFIRMED);
        saveHistory(booking, BookingStatus.PENDING, BookingStatus.CONFIRMED,
                "Booking confirmed by staff");

        return toResponse(bookingRepository.save(booking));
    }

    // ─────────────────────────────────────────────────────────────
    // 3. PUT /api/admin/bookings/{id}/check-in
    //    Validate: chỉ check-in đúng ngày checkInDate
    // ─────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────
    // 4. PUT /api/admin/bookings/{id}/check-out
    // ─────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────
    // 5. PUT /api/admin/bookings/{id}/cancel — Admin hủy bất kỳ
    // ─────────────────────────────────────────────────────────────
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

        saveHistory(booking, oldStatus, BookingStatus.CANCELLED,
                "Cancelled by admin. Reason: " + reason);

        return toResponse(bookingRepository.save(booking));
    }

    // ─────────────────────────────────────────────────────────────
    // 6. PUT /api/admin/bookings/{id}/no-show
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BookingResponse markNoShow(Long id) {
        Booking booking = findOrThrow(id);
        validateTransition(booking, BookingStatus.CONFIRMED, BookingStatus.NO_SHOW);

        booking.setStatus(BookingStatus.NO_SHOW);
        saveHistory(booking, BookingStatus.CONFIRMED, BookingStatus.NO_SHOW,
                "Marked as no-show by staff");

        return toResponse(bookingRepository.save(booking));
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

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
        history.setChangedBy(actor);           // ✅ User object — khớp entity của bạn
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    // ─────────────────────────────────────────────────────────────
    // Mapper: Booking → BookingResponse
    // ✅ FIX: không dùng builder() — dùng new + setter vì BookingResponse của bạn dùng @Data
    // ✅ FIX: dùng BookingStatusHistoryResponse (class của bạn), không phải StatusHistoryResponse
    // ─────────────────────────────────────────────────────────────
    private BookingResponse toResponse(Booking b) {

        // Map status histories
        List<BookingStatusHistoryResponse> histories =
                historyRepository.findByBookingIdOrderByChangedAtDesc(b.getId())
                        .stream()
                        .map(h -> {
                            BookingStatusHistoryResponse hr = new BookingStatusHistoryResponse();
                            hr.setOldStatus(h.getOldStatus());
                            hr.setNewStatus(h.getNewStatus());
                            // changedBy là User object → lấy fullName để hiển thị
                            hr.setChangedBy(h.getChangedBy() != null
                                    ? h.getChangedBy().getFullName() : null);
                            hr.setNote(h.getNote());
                            hr.setChangedAt(h.getChangedAt());
                            return hr;
                        })
                        .toList();

        // ✅ FIX: dùng new BookingResponse() + setter, không dùng .builder()
        BookingResponse res = new BookingResponse();
        res.setId(b.getId());
        res.setCheckInDate(b.getCheckInDate());
        res.setCheckOutDate(b.getCheckOutDate());
        res.setNumGuests(b.getNumGuests());
        res.setTotalAmount(b.getTotalAmount());
        res.setStatus(b.getStatus());
        res.setSpecialRequests(b.getSpecialRequests());
        res.setCreatedAt(b.getCreatedAt());

        // BookingResponse của bạn nhận UserResponse và RoomResponse
        // → ModelMapper xử lý ở BookingService.createBooking()
        // → Ở đây map thủ công để tránh lazy-load
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