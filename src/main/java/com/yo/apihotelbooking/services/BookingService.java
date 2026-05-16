package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.dto.response.PricingEstimateResponse;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import com.yo.apihotelbooking.schemas.domain.Room;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.schemas.enums.UserRole;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.BookingStatusHistoryRepository;
import com.yo.apihotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import com.yo.apihotelbooking.repository.UserRepository;
import org.springframework.data.domain.Page;
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PricingService pricingService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final BookingStatusHistoryRepository historyRepository;

@Transactional(rollbackFor = Exception.class)
public BookingResponse createBooking(BookingRequest request, Long userId) throws Exception {
    Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng"));
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    List<Booking> conflicts = bookingRepository.findConflictingBookings(
            room.getId(), 
            request.getCheckInDate(), 
            request.getCheckOutDate()
    );

    if (!conflicts.isEmpty()) {
        throw new BadRequestException("Phòng đã có người đặt hoặc đang chờ xác nhận trong khoảng thời gian này.");
    }

    PricingEstimateResponse pricing = pricingService.estimatePrice(
            room.getId(), request.getCheckInDate(), request.getCheckOutDate());

    Booking booking = new Booking();
    booking.setRoom(room);
    booking.setUser(user);
    booking.setCheckInDate(request.getCheckInDate());
    booking.setCheckOutDate(request.getCheckOutDate());
    booking.setTotalAmount(pricing.getTotalAmount());
    booking.setStatus(BookingStatus.PENDING);
    booking.setSpecialRequests(request.getSpecialRequests());
    booking.setNumGuests(request.getNumGuests() != null ? request.getNumGuests() : 1);
    Booking savedBooking = bookingRepository.save(booking);

    return modelMapper.map(savedBooking, BookingResponse.class);
}
    @Transactional
public void updateBookingStatus(Long bookingId, BookingStatus newStatus, User performer, String note) throws NotFoundException {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));

    BookingStatus oldStatus = booking.getStatus();
    
    booking.setStatus(newStatus);
    bookingRepository.save(booking);
    
    BookingStatusHistory history = new BookingStatusHistory();
    history.setBooking(booking);
    history.setOldStatus(oldStatus);
    history.setNewStatus(newStatus);
    history.setChangedBy(performer);
    history.setNote(note);
    history.setChangedAt(LocalDateTime.now());
    historyRepository.save(history);
}

public Page<Booking> getMyBookings(User currentUser, int page, int size) {
        return bookingRepository.findByUserId(currentUser.getId(), PageRequest.of(page, size));
    }

public Booking getBookingDetail(Long id) {
        User currentUser = SecurityUtils.getCurrentUser(); //
        if (currentUser == null) throw new RuntimeException("Bạn chưa đăng nhập!");

        Booking booking = bookingRepository.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt phòng!"));

        if (!(booking.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.STAFF)) {
            throw new RuntimeException("Bạn không có quyền xem thông tin này!");
        }
        return booking;
    }


   @Transactional
    public void cancelBooking(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
 
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng!"));
 
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn này!");
        }
 
        // Chỉ cho hủy khi đang PENDING hoặc CONFIRMED
        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hủy đơn khi đang ở trạng thái PENDING hoặc CONFIRMED!");
        }
 
        LocalDateTime now      = LocalDateTime.now();
        // Deadline: trước 24h so với checkInDate
        LocalDateTime deadline = booking.getCheckInDate().atStartOfDay().minusHours(24);
 
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now);
 
        String note;
        if (now.isBefore(deadline)) {
            // Hủy trong hạn → đủ điều kiện hoàn tiền
            booking.setCancellationReason("Khách hủy trong hạn — đủ điều kiện hoàn tiền");
            note = "Customer cancelled before deadline — refund eligible";
            // TODO: trigger refund event
        } else {
            // Hủy trễ → không hoàn tiền
            booking.setCancellationReason("Khách hủy trễ (< 24h trước check-in) — không hoàn tiền");
            note = "Customer cancelled after deadline — no refund";
        }
 
        bookingRepository.save(booking);
        saveHistory(booking, oldStatus, BookingStatus.CANCELLED, currentUser, note);
    }
 
    // ─────────────────────────────────────────────────────────────
    // Helper: tạo và lưu 1 dòng status history
    // ─────────────────────────────────────────────────────────────
    private void saveHistory(Booking booking, BookingStatus oldStatus,
                             BookingStatus newStatus, User changedBy, String note) {
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);    // ✅ User object — khớp với entity của bạn
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }
}