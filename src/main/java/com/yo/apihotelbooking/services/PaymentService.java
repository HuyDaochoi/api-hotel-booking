package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.dto.request.PaymentFilterRequest;
import com.yo.apihotelbooking.dto.response.PaymentResponse;
import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.PaymentRepository;
import com.yo.apihotelbooking.schemas.domain.Booking;
import com.yo.apihotelbooking.schemas.domain.Payment;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    /**
     * Lấy danh sách giao dịch theo mã Booking.
     * Khách hàng chỉ xem được đơn của mình. Staff/Admin xem được tất cả.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        // 1. Kiểm tra session người dùng hiện tại
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Bạn chưa đăng nhập vào hệ thống.");
        }

        // 2. Tìm kiếm Booking - Ném NotFoundException (404) nếu không tồn tại
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin đặt phòng với ID: " + bookingId));

        // 3. Kiểm tra phân quyền sở hữu hoặc quyền hạn quản trị
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == UserRole.STAFF || currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isStaffOrAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin thanh toán của đơn đặt phòng này.");
        }

        // 4. Trả về danh sách đã được map phẳng qua DTO
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * [ADMIN/STAFF Only] Truy vấn toàn bộ danh sách giao dịch kèm bộ lọc động và phân trang.
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPaymentsForAdmin(PaymentFilterRequest filter, int page, int size) {
        // Chuẩn hóa dữ liệu ngày tháng sang LocalDateTime phục vụ truy vấn DB
        LocalDateTime startDateTime = filter.getFromDate() != null ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime endDateTime = filter.getToDate() != null ? filter.getToDate().atTime(23, 59, 59) : null;

        // Cấu hình phân trang, ưu tiên hiển thị giao dịch mới nhất
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

    /**
     * [ADMIN/STAFF Only] Xem chi tiết một hóa đơn giao dịch cụ thể qua ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(Long id) {
        // Tìm kiếm thông tin Payment - Ném lỗi NotFoundException (404) nếu sai mã ID
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mã giao dịch thanh toán không tồn tại: id=" + id));
        return convertToResponse(payment);
    }

    /**
     * Phương thức nội bộ chuyển đổi từ Entity sang DTO an toàn thông tin.
     */
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
}