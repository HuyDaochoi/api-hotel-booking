package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.dto.request.PaymentFilterRequest;
import com.yo.apihotelbooking.dto.response.PaymentResponse;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.BookingStatusHistoryRepository;
import com.yo.apihotelbooking.repository.PaymentRepository;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.BookingStatusHistory;
import com.yo.apihotelbooking.schemas.domain.Payment;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yo.apihotelbooking.common.exception.BadRequestException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final BookingService bookingService;

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) throw new AccessDeniedException("Bạn chưa đăng nhập.");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin đặt phòng"));

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"));

        if (!isOwner && !isStaffOrAdmin) throw new AccessDeniedException("Không có quyền.");

        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPaymentsForAdmin(PaymentFilterRequest filter, int page, int size) {
        LocalDateTime startDateTime = filter.getFromDate() != null ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime endDateTime = filter.getToDate() != null ? filter.getToDate().atTime(23, 59, 59) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("processedAt").descending());

        return paymentRepository.findAllWithFilter(
                filter.getBookingId(), filter.getPaymentType(), filter.getPaymentMethod(),
                filter.getStatus(), startDateTime, endDateTime, pageable
        ).map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Giao dịch không tồn tại"));
        return convertToResponse(payment);
    }

    private PaymentResponse convertToResponse(Payment p) {
        PaymentResponse res = new PaymentResponse();
        res.setId(p.getId());
        res.setAmount(p.getAmount());
        res.setPaymentType(p.getPaymentType());
        res.setPaymentMethod(p.getPaymentMethod());
        res.setStatus(p.getStatus());
        res.setTransactionRef(p.getTransactionRef());
        res.setProcessedAt(p.getProcessedAt());
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse processPaymentSuccess(Long paymentId, String transactionRef, String note) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return convertToResponse(payment);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setAmountPaid(payment.getAmount());
        payment.setProcessedAt(LocalDateTime.now());
        if (transactionRef != null && !transactionRef.isBlank()) payment.setTransactionRef(transactionRef);
        if (note != null) payment.setNote(note);

        Payment savedPayment = paymentRepository.saveAndFlush(payment);
        Booking booking = payment.getBooking();

        if (booking.getStatus() == BookingStatus.PENDING) {
            BookingStatus oldStatus = booking.getStatus();
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.saveAndFlush(booking);

            User currentUser = SecurityUtils.getCurrentUser();
            BookingStatusHistory history = new BookingStatusHistory();
            history.setBooking(booking);
            history.setOldStatus(oldStatus);
            history.setNewStatus(BookingStatus.CONFIRMED);
            history.setChangedBy(currentUser);
            history.setNote("Hệ thống tự động xác nhận đơn do thanh toán thành công.");
            history.setChangedAt(LocalDateTime.now());
            historyRepository.save(history);
        }

        bookingService.recalculatePaymentStatus(booking.getId());
        return convertToResponse(savedPayment);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse processPaymentFailed(Long paymentId, String failureReason) throws BadRequestException, NotFoundException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể đánh dấu thất bại cho giao dịch đang PENDING.");
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setAmountPaid(BigDecimal.ZERO);
        payment.setProcessedAt(LocalDateTime.now());
        payment.setNote("Giao dịch thất bại: " + failureReason);

        return convertToResponse(paymentRepository.saveAndFlush(payment));
    }
}