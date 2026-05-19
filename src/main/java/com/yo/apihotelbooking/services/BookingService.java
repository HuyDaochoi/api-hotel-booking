package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.BookingResponse;
import com.yo.apihotelbooking.dto.response.BookingStatusHistoryResponse;
import com.yo.apihotelbooking.dto.response.RoomResponse;
import com.yo.apihotelbooking.dto.response.RoomTypeResponse;
import com.yo.apihotelbooking.dto.response.UserResponse;
import com.yo.apihotelbooking.dto.response.PricingEstimateResponse;
import com.yo.apihotelbooking.schemas.domain.*;
import com.yo.apihotelbooking.schemas.enums.*;
import com.yo.apihotelbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository              bookingRepository;
    private final RoomRepository                 roomRepository;
    private final UserRepository                 userRepository;
    private final PaymentRepository              paymentRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final PricingService                 pricingService;

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse createBooking(BookingRequest request, Long userId) throws Exception {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

      
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!conflicts.isEmpty()) {
            throw new BadRequestException(
                    "Phòng đã có người đặt hoặc đang chờ xác nhận trong khoảng thời gian này.");
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
        Booking saved = bookingRepository.save(booking);

        saveHistory(saved, null, BookingStatus.PENDING, user, "Booking created");

  
        Payment payment = new Payment();
        payment.setBooking(saved);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setAmount(saved.getTotalAmount());
        payment.setPaymentMethod(PaymentMethod.SIMULATED);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setNote("Auto payment on booking creation");
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return toResponse(saved);
    }

    public Page<BookingResponse> getMyBookings(User currentUser, int page, int size) {
        return bookingRepository
                .findByUserId(currentUser.getId(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    public BookingResponse getBookingDetail(Long id) throws NotFoundException, BadRequestException {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("Bạn chưa đăng nhập");
        }

        Booking booking = bookingRepository.findDetailById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đặt phòng"));

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == UserRole.ADMIN
                       || currentUser.getRole() == UserRole.STAFF;
        if (!isOwner && !isStaff) {
            throw new BadRequestException("Bạn không có quyền xem thông tin này");
        }

        return toResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long id) throws BadRequestException, NotFoundException {
        User currentUser = SecurityUtils.getCurrentUser();

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn không có quyền hủy đơn này");
        }

        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Chỉ hủy được khi booking đang PENDING hoặc CONFIRMED");
        }

        long deadlineHours = parseDeadlineHours(
                booking.getRoom().getRoomType().getCancellationPolicy(), 24L);

        LocalDateTime now      = LocalDateTime.now();
        LocalDateTime deadline = booking.getCheckInDate().atStartOfDay().minusHours(deadlineHours);

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now);

        if (now.isBefore(deadline)) {
            booking.setCancellationReason("Hủy trong hạn — hoàn tiền 100%");
            bookingRepository.save(booking);
            saveHistory(booking, oldStatus, BookingStatus.CANCELLED, currentUser,
                    "Customer cancelled before deadline — refund 100%");

            Payment refund = new Payment();
            refund.setBooking(booking);
            refund.setPaymentType(PaymentType.REFUND);
            refund.setAmount(booking.getTotalAmount());
            refund.setPaymentMethod(PaymentMethod.SIMULATED);
            refund.setStatus(PaymentStatus.SUCCESS);
            refund.setTransactionRef("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            refund.setNote("Auto refund — cancelled before deadline (" + deadlineHours + "h)");
            refund.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(refund);

        } else {
            booking.setCancellationReason(
                    "Hủy trễ (< " + deadlineHours + "h trước check-in) — không hoàn tiền");
            bookingRepository.save(booking);
            saveHistory(booking, oldStatus, BookingStatus.CANCELLED, currentUser,
                    "Customer cancelled after deadline — no refund");
        }
    }

    @Transactional
    public void updateBookingStatus(Long bookingId, BookingStatus newStatus,
                                    User performer, String note) throws NotFoundException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        saveHistory(booking, oldStatus, newStatus, performer, note);
    }
    private void saveHistory(Booking booking, BookingStatus oldStatus,
                             BookingStatus newStatus, User changedBy, String note) {
        BookingStatusHistory h = new BookingStatusHistory();
        h.setBooking(booking);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setChangedBy(changedBy);
        h.setNote(note);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);
    }

    private long parseDeadlineHours(String policy, long defaultHours) {
        if (policy == null || policy.isBlank()) return defaultHours;
        try {
            String digits = policy.replaceAll("[^0-9]", "");
            return digits.isBlank() ? defaultHours : Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return defaultHours;
        }
    }

  
    public BookingResponse toResponse(Booking b) {
        List<BookingStatusHistoryResponse> histories =
                historyRepository.findByBookingIdOrderByChangedAtDesc(b.getId())
                        .stream()
                        .map(h -> {
                            BookingStatusHistoryResponse hr = new BookingStatusHistoryResponse();
                            hr.setOldStatus(h.getOldStatus());
                            hr.setNewStatus(h.getNewStatus());
                            hr.setChangedBy(h.getChangedBy() != null
                                    ? h.getChangedBy().getFullName() : "System");
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
        res.setStatusHistories(histories);

        if (b.getUser() != null) {
            UserResponse u = new UserResponse();
            u.setId(b.getUser().getId());
            u.setEmail(b.getUser().getEmail());
            u.setUsername(b.getUser().getUsername());
            u.setFullName(b.getUser().getFullName());
            u.setPhone(b.getUser().getPhone());
            u.setRole(b.getUser().getRole());
            u.setIsActive(b.getUser().getIsActive());
            res.setUser(u);
        }

        if (b.getRoom() != null) {
            RoomResponse r = new RoomResponse();
            r.setId(b.getRoom().getId());
            r.setRoomNumber(b.getRoom().getRoomNumber());
            r.setFloor(b.getRoom().getFloor());

            if (b.getRoom().getRoomType() != null) {
                RoomTypeResponse rt = new RoomTypeResponse();
                rt.setId(b.getRoom().getRoomType().getId());
                rt.setName(b.getRoom().getRoomType().getName());
                rt.setBasePrice(b.getRoom().getRoomType().getBasePrice());
                rt.setMaxCapacity(b.getRoom().getRoomType().getMaxCapacity());
                rt.setCancellationPolicy(b.getRoom().getRoomType().getCancellationPolicy());
                rt.setIsActive(b.getRoom().getRoomType().getIsActive());
                r.setRoomType(rt);
            }
            res.setRoom(r);
        }

        return res;
    }
}