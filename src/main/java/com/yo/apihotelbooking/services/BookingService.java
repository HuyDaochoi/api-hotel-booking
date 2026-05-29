package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.dto.request.BookingRequest;
import com.yo.apihotelbooking.dto.response.*;
import com.yo.apihotelbooking.schemas.domain.*;
import com.yo.apihotelbooking.schemas.enums.*;
import com.yo.apihotelbooking.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final PricingService pricingService;
    private final BookingRuleRepository bookingRuleRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final EntityManager entityManager;

    @Transactional(rollbackFor = Exception.class)
    public void recalculatePaymentStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));

        List<Payment> successPayments = paymentRepository.findByBookingId(bookingId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .toList();

        // Tổng tiền khách thực tế đã thanh toán thành công
        BigDecimal totalPaid = successPayments.stream()
                .filter(p -> p.getPaymentType() == PaymentType.PAYMENT)
                .map(p -> p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền hệ thống đã hoàn trả thành công (nếu có)
        BigDecimal totalRefunded = successPayments.stream()
                .filter(p -> p.getPaymentType() == PaymentType.REFUND)
                .map(p -> p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Số tiền thực tế sau khi đối soát đối trừ tiền hoàn
        BigDecimal netAmount = totalPaid.subtract(totalRefunded);

        BigDecimal targetTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
                booking.setBookingPaymentStatus(BookingPaymentStatus.UNPAID);
            } else if (totalRefunded.compareTo(BigDecimal.ZERO) > 0 && netAmount.compareTo(BigDecimal.ZERO) > 0) {
                booking.setBookingPaymentStatus(BookingPaymentStatus.PARTIAL_REFUNDED);
            } else {
                booking.setBookingPaymentStatus(BookingPaymentStatus.REFUNDED);
            }
        } else {
            // Luồng cập nhật trạng thái khi đơn phòng đang hoạt động bình thường
            if (netAmount.compareTo(BigDecimal.ZERO) == 0) {
                booking.setBookingPaymentStatus(BookingPaymentStatus.UNPAID);
            } else if (netAmount.compareTo(targetTotal) >= 0) {
                booking.setBookingPaymentStatus(BookingPaymentStatus.PAID);
            } else {
                // Đã cọc thành công số tiền cọc (ví dụ 30%) nhưng chưa thanh toán hết toàn bộ đơn 100%
                booking.setBookingPaymentStatus(BookingPaymentStatus.PARTIAL);
            }
        }

        bookingRepository.saveAndFlush(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookingRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingDetail(Long id) throws NotFoundException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Đơn đặt phòng không tồn tại"));

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn chưa đăng nhập");
        }

        boolean isStaffOrAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"));

        if (!booking.getUser().getId().equals(currentUser.getId()) && !isStaffOrAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền xem thông tin đơn đặt phòng này");
        }
        return toResponse(booking);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse cancelBooking(Long id) throws NotFoundException, BadRequestException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));

        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn chưa đăng nhập");
        }

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền hủy đơn đặt phòng này");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Không thể hủy phòng khi đang ở trạng thái: " + booking.getStatus());
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason("Hủy bởi khách hàng");

        LocalDateTime checkInDateTime = booking.getCheckInDate().atTime(14, 0);
        long hoursUntilCheckIn = ChronoUnit.HOURS.between(LocalDateTime.now(), checkInDateTime);

        Long roomTypeId = booking.getRoom().getRoomType().getId();
        BigDecimal refundPercent = getRefundPercentByPolicy(roomTypeId, hoursUntilCheckIn);

        List<Payment> payments = paymentRepository.findByBookingId(id);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && p.getPaymentType() == PaymentType.PAYMENT)
                .map(p -> p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal refundAmount = BigDecimal.ZERO;
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0 && refundPercent.compareTo(BigDecimal.ZERO) > 0) {
            refundAmount = totalPaid.multiply(refundPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                Payment refund = new Payment();
                refund.setBooking(booking);
                refund.setPaymentType(PaymentType.REFUND);
                refund.setAmount(refundAmount);
                refund.setAmountPaid(refundAmount);
                refund.setPaymentMethod(PaymentMethod.SIMULATED);
                refund.setStatus(PaymentStatus.SUCCESS);
                refund.setTransactionRef("REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                refund.setNote("Hoàn trả " + refundPercent + "% tiền cọc theo chính sách hủy phòng trước " + hoursUntilCheckIn + " giờ.");
                refund.setProcessedAt(LocalDateTime.now());
                paymentRepository.save(refund);
            }
        }

        bookingRepository.saveAndFlush(booking);

        BookingStatusHistory h = new BookingStatusHistory();
        h.setBooking(booking);
        h.setOldStatus(oldStatus);
        h.setNewStatus(BookingStatus.CANCELLED);
        h.setChangedBy(currentUser);
        h.setNote("Khách hàng chủ động hủy đơn. Hệ thống áp dụng chính sách tự động hoàn trả: " + refundAmount + " VNĐ (" + refundPercent + "%)");
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);

        recalculatePaymentStatus(id);
        entityManager.refresh(booking);

        return toResponse(booking);
    }

    public BigDecimal getRefundPercentByPolicy(Long roomTypeId, long hoursUntilCheckIn) {
        List<CancellationPolicy> policies = cancellationPolicyRepository.findByRoomTypeId(roomTypeId);
        if (policies == null || policies.isEmpty()) {
            policies = cancellationPolicyRepository.findDefaultPolicies();
        }

        BigDecimal refundPercent = BigDecimal.ZERO;
        CancellationPolicy matchedPolicy = null;

        for (CancellationPolicy policy : policies) {
            if (hoursUntilCheckIn >= policy.getHoursBeforeCheckin()) {
                if (matchedPolicy == null || policy.getHoursBeforeCheckin() > matchedPolicy.getHoursBeforeCheckin()) {
                    matchedPolicy = policy;
                    refundPercent = policy.getRefundPercent();
                }
            }
        }
        return refundPercent;
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse createBooking(BookingRequest request, Long userId) throws BadRequestException, NotFoundException {
        Room room = roomRepository.findByIdWithLock(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phòng"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        BookingRule rule = bookingRuleRepository.findFirstByRoomTypeId(room.getRoomType().getId())
                .orElse(bookingRuleRepository.findDefaultRule());

        PricingEstimateResponse pricing = pricingService.estimatePrice(room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = pricing.getTotalAmount();
        BigDecimal requiredAmount = totalAmount; // Mặc định thu 100% tổng tiền nếu rule không cấu hình cọc

        if (rule != null) {
            long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
            if (nights < rule.getMinNights()) {
                throw new BadRequestException("Phòng yêu cầu tối thiểu đặt " + rule.getMinNights() + " đêm");
            }
            if (request.getCheckInDate().isBefore(LocalDate.now().plusDays(rule.getMinDaysAdvance()))) {
                throw new BadRequestException("Cần đặt phòng trước ít nhất " + rule.getMinDaysAdvance() + " ngày");
            }
            if (rule.getDepositPercentage() != null && rule.getDepositPercentage().compareTo(BigDecimal.ZERO) > 0) {
                // Tính toán chính xác số tiền cọc dựa trên phần trăm cấu hình (Ví dụ: 30%)
                requiredAmount = totalAmount.multiply(rule.getDepositPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Phòng đã có người đặt trong khoảng thời gian này");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalAmount(totalAmount);
        booking.setDepositAmount(requiredAmount);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingPaymentStatus(BookingPaymentStatus.UNPAID);
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setNumGuests(request.getNumGuests() != null ? request.getNumGuests() : 1);
        Booking saved = bookingRepository.saveAndFlush(booking);

        saveHistory(saved, null, BookingStatus.PENDING, user, "Booking created via client request");

        Payment payment = new Payment();
        payment.setBooking(saved);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setAmount(requiredAmount);
        payment.setAmountPaid(BigDecimal.ZERO);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return toResponse(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus,
                                               User performer, String note) throws NotFoundException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt phòng"));

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(newStatus);

        if (newStatus == BookingStatus.CONFIRMED) {
            List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
            if (payments != null) {
                for (Payment p : payments) {
                    if (p.getPaymentType() == PaymentType.PAYMENT && p.getStatus() == PaymentStatus.PENDING) {
                        p.setStatus(PaymentStatus.SUCCESS);
                        p.setAmountPaid(p.getAmount());
                        p.setProcessedAt(LocalDateTime.now());
                        paymentRepository.save(p);
                    }
                }
            }
        } else if (newStatus == BookingStatus.CHECKED_IN) {
            booking.setCheckedInAt(LocalDateTime.now());
        } else if (newStatus == BookingStatus.CHECKED_OUT) {
            booking.setCheckedOutAt(LocalDateTime.now());

            List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
            BigDecimal totalPaid = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS && p.getPaymentType() == PaymentType.PAYMENT)
                    .map(p -> p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remainingAmount = booking.getTotalAmount().subtract(totalPaid);

            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                Payment finalPayment = new Payment();
                finalPayment.setBooking(booking);
                finalPayment.setPaymentType(PaymentType.PAYMENT);
                finalPayment.setAmount(remainingAmount);
                finalPayment.setAmountPaid(remainingAmount);
                finalPayment.setPaymentMethod(PaymentMethod.SIMULATED);
                finalPayment.setStatus(PaymentStatus.SUCCESS);
                finalPayment.setTransactionRef("OUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                finalPayment.setProcessedAt(LocalDateTime.now());
                finalPayment.setNote("Thanh toán nốt số tiền thiếu khi Check-out tại quầy.");
                paymentRepository.save(finalPayment);
            }
        }

        bookingRepository.saveAndFlush(booking);
        saveHistory(booking, oldStatus, newStatus, performer, note);
        recalculatePaymentStatus(bookingId);
        entityManager.refresh(booking);

        return toResponse(booking);
    }

    public void saveHistory(Booking booking, BookingStatus oldStatus, BookingStatus newStatus, User changedBy, String note) {
        BookingStatusHistory h = new BookingStatusHistory();
        h.setBooking(booking);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setChangedBy(changedBy);
        h.setNote(note);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);
    }

    public BookingResponse toResponse(Booking b) {
        BookingResponse res = new BookingResponse();
        res.setId(b.getId());
        res.setCheckInDate(b.getCheckInDate());
        res.setCheckOutDate(b.getCheckOutDate());
        res.setTotalAmount(b.getTotalAmount());
        res.setStatus(b.getStatus());
        res.setBookingPaymentStatus(b.getBookingPaymentStatus());
        res.setNumGuests(b.getNumGuests());
        res.setCreatedAt(b.getCreatedAt());
        res.setCancellationReason(b.getCancellationReason());

        if (b.getId() != null && historyRepository != null) {
            historyRepository.findByBookingIdOrderByChangedAtDesc(b.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(latestHistory -> res.setLastStatusNote(latestHistory.getNote()));
        }

        if (b.getUser() != null) {
            UserResponse u = new UserResponse();
            u.setId(b.getUser().getId());
            u.setFullName(b.getUser().getFullName());
            u.setEmail(b.getUser().getEmail());
            res.setUser(u);
        }

        if (b.getRoom() != null) {
            RoomResponse r = new RoomResponse();
            r.setId(b.getRoom().getId());
            r.setRoomNumber(b.getRoom().getRoomNumber());
            r.setFloor(b.getRoom().getFloor());

            if (b.getRoom().getRoomType() != null) {
                RoomType rt = b.getRoom().getRoomType();
                RoomTypeResponse rtr = new RoomTypeResponse();
                rtr.setId(rt.getId());
                rtr.setName(rt.getName());
                r.setRoomType(rtr);
            }
            res.setRoom(r);
        }
        return res;
    }
}