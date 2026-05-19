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
import com.yo.apihotelbooking.schemas.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Bạn chưa đăng nhập vào hệ thống.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin đặt phòng với ID: " + bookingId));

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == UserRole.STAFF || currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isStaffOrAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin thanh toán của đơn đặt phòng này.");
        }

        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPaymentsForAdmin(PaymentFilterRequest filter, int page, int size) {
        LocalDateTime startDateTime = filter.getFromDate() != null ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime endDateTime = filter.getToDate() != null ? filter.getToDate().atTime(23, 59, 59) : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("processedAt").descending());

        Page<Payment> paymentPage = paymentRepository.findAllWithFilter(
                filter.getBookingId(),
                filter.getPaymentType(),
                filter.getPaymentMethod(),
                filter.getStatus(),
                startDateTime,
                endDateTime,
                pageable
        );

        return paymentPage.map(this::convertToResponse);
    }

 
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mã giao dịch thanh toán không tồn tại: id=" + id));
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
    // 1. Tìm hóa đơn thanh toán
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch với ID: " + paymentId));

    // Khóa an toàn: Nếu hóa đơn đã thành công hoặc đã refund thì không xử lý lại
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
        return convertToResponse(payment);
    }

    // 2. Cập nhật trạng thái hóa đơn thanh toán
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setProcessedAt(LocalDateTime.now());
    if (transactionRef != null && !transactionRef.isBlank()) {
        payment.setTransactionRef(transactionRef);
    }
    if (note != null) {
        payment.setNote(note);
    }
    Payment savedPayment = paymentRepository.save(payment);

    // 3. ĐỒNG BỘ SANG BOOKING: Chuyển đơn đặt phòng sang CONFIRMED (Đã xác nhận)
    Booking booking = payment.getBooking();
    if (booking.getStatus() == BookingStatus.PENDING) {
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // 4. Ghi nhận lịch sử thay đổi trạng thái đơn đặt phòng
        User currentUser = SecurityUtils.getCurrentUser(); // Có thể là Admin hoặc System xử lý webhook
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setOldStatus(oldStatus);
        history.setNewStatus(BookingStatus.CONFIRMED);
        history.setChangedBy(currentUser);
        history.setNote("Hệ thống tự động xác nhận đơn do thanh toán thành công. Giao dịch: " + savedPayment.getTransactionRef());
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    return convertToResponse(savedPayment);
}

@Transactional(rollbackFor = Exception.class)
public PaymentResponse processPaymentFailed(Long paymentId, String failureReason) throws BadRequestException,NotFoundException{
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy giao dịch với ID: " + paymentId));

    if (payment.getStatus() != PaymentStatus.PENDING) {
        throw new BadRequestException("Chỉ có thể đánh dấu thất bại cho giao dịch đang ở trạng thái PENDING.");
    }

    payment.setStatus(PaymentStatus.FAILED);
    payment.setProcessedAt(LocalDateTime.now());
    payment.setNote("Giao dịch thất bại. Lý do: " + failureReason);
    

    return convertToResponse(paymentRepository.save(payment));
}
}