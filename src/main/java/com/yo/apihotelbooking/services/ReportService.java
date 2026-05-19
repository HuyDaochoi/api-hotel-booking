package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.repository.BookingRepository;
import com.yo.apihotelbooking.repository.PaymentRepository;
import com.yo.apihotelbooking.repository.RoomRepository;
import com.yo.apihotelbooking.schemas.enums.BookingStatus;
import com.yo.apihotelbooking.schemas.enums.PaymentType;
import com.yo.apihotelbooking.schemas.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;

    // ─────────────────────────────────────────────────────────────
    // 1. Tỉ lệ lấp đầy (Occupancy)
    //    GET /api/admin/reports/occupancy?from=...&to=...
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getOccupancy(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now();
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from phải trước hoặc bằng to");
        }

        long totalRooms = roomRepository.count();
        if (totalRooms == 0) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("from", from);
            empty.put("to", to);
            empty.put("totalRooms", 0);
            empty.put("occupancyPercent", 0);
            empty.put("note", "Chưa có phòng nào trong hệ thống");
            return empty;
        }

        final LocalDate finalFrom = from;
        final LocalDate finalTo = to;

        // Tính số booking active (không CANCELLED/NO_SHOW) chồng lên khoảng [from, to]
        long activeBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED
                        && b.getStatus() != BookingStatus.NO_SHOW)
                .filter(b -> b.getCheckInDate() != null && b.getCheckOutDate() != null)
                .filter(b -> !b.getCheckInDate().isAfter(finalTo)
                        && !b.getCheckOutDate().isBefore(finalFrom))
                .count();

        long totalDays = to.toEpochDay() - from.toEpochDay() + 1;
        // Tỉ lệ = (số booking active) / (tổng phòng × số ngày) × 100
        // NOTE: đây là ước tính, mỗi booking chỉ tính 1 room cho toàn bộ khoảng ngày
        BigDecimal occupancyPercent = BigDecimal.ZERO;
        long denominator = totalRooms * totalDays;
        if (denominator > 0) {
            occupancyPercent = BigDecimal.valueOf(activeBookings * 100L)
                    .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from);
        result.put("to", to);
        result.put("totalRooms", totalRooms);
        result.put("totalDays", totalDays);
        result.put("activeBookings", activeBookings);
        result.put("occupancyPercent", occupancyPercent);
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Doanh thu (Revenue)
    //    GET /api/admin/reports/revenue?from=...&to=...&groupBy=month|week
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenue(LocalDate from, LocalDate to, String groupBy) {
        if (from == null) from = LocalDate.now().minusMonths(6);
        if (to == null) to = LocalDate.now();
        if (groupBy == null || groupBy.isBlank()) groupBy = "month";

        final LocalDate finalFrom = from;
        final LocalDate finalTo = to;

        // Lọc payments trong khoảng ngày có processedAt
        var payments = paymentRepository.findAll().stream()
                .filter(p -> p.getProcessedAt() != null
                        && p.getStatus() == PaymentStatus.SUCCESS)
                .filter(p -> {
                    LocalDate pDate = p.getProcessedAt().toLocalDate();
                    return !pDate.isBefore(finalFrom) && !pDate.isAfter(finalTo);
                })
                .toList();

        BigDecimal totalPayment = payments.stream()
                .filter(p -> p.getPaymentType() == PaymentType.PAYMENT)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefund = payments.stream()
                .filter(p -> p.getPaymentType() == PaymentType.REFUND)
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = totalPayment.subtract(totalRefund);

        // Group by month hoặc week
        // NOTE: groupBy=day chưa implement
        final String finalGroupBy = groupBy;
        Map<String, BigDecimal> grouped = payments.stream()
                .filter(p -> p.getPaymentType() == PaymentType.PAYMENT)
                .collect(Collectors.groupingBy(
                        p -> {
                            LocalDate d = p.getProcessedAt().toLocalDate();
                            if ("week".equalsIgnoreCase(finalGroupBy)) {
                                // Lấy số tuần trong năm
                                return d.getYear() + "-W"
                                        + String.format("%02d", d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                            }
                            // default: month
                            return d.getYear() + "-" + String.format("%02d", d.getMonthValue());
                        },
                        Collectors.reducing(BigDecimal.ZERO,
                                p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from);
        result.put("to", to);
        result.put("groupBy", groupBy);
        result.put("totalPayment", totalPayment);
        result.put("totalRefund", totalRefund);
        result.put("netRevenue", netRevenue);
        result.put("transactionCount", payments.size());
        result.put("breakdown", new TreeMap<>(grouped));
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // 3. Loại phòng được đặt nhiều nhất
    //    GET /api/admin/reports/popular-room-types?from=...&to=...&limit=5
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopularRoomTypes(LocalDate from, LocalDate to, int limit) {
        if (from == null) from = LocalDate.now().minusMonths(3);
        if (to == null) to = LocalDate.now();
        if (limit <= 0 || limit > 50) limit = 5;

        final LocalDate finalFrom = from;
        final LocalDate finalTo = to;

        // Đếm số booking theo roomType trong khoảng thời gian
        // NOTE: sort theo số booking, chưa phải số đêm (xem note ở đầu file)
        Map<String, Long> countByType = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED
                        && b.getStatus() != BookingStatus.NO_SHOW)
                .filter(b -> b.getCheckInDate() != null
                        && !b.getCheckInDate().isBefore(finalFrom)
                        && !b.getCheckInDate().isAfter(finalTo))
                .filter(b -> b.getRoom() != null && b.getRoom().getRoomType() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getRoom().getRoomType().getName(),
                        Collectors.counting()
                ));

        return countByType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("roomTypeName", e.getKey());
                    item.put("bookingCount", e.getValue());
                    return item;
                })
                .toList();
    }
}
