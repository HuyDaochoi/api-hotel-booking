package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// ─────────────────────────────────────────────────────────────
// NOTE (chưa sửa):
//   - Tất cả 3 report đều load toàn bộ data từ DB rồi filter/group
//     trong Java → performance kém khi data lớn. Cần chuyển sang
//     @Query JPQL hoặc native SQL aggregate sau này.
//   - Chưa có cache (Redis/Caffeine) → mỗi request đều query DB
//   - groupBy=day ở revenue chưa implement (xem note trong ReportService)
// ─────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class ReportController {

    private final ReportService reportService;

    // GET /api/admin/reports/occupancy?from=2026-01-01&to=2026-05-31
    // Role: ADMIN, STAFF
    @GetMapping("/occupancy")
    public ApiResponse<Map<String, Object>> getOccupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success("Tỉ lệ lấp đầy", reportService.getOccupancy(from, to));
    }

    // GET /api/admin/reports/revenue?from=2026-01-01&to=2026-05-31&groupBy=month
    // Role: ADMIN only
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ApiResponse.success("Doanh thu theo giai đoạn", reportService.getRevenue(from, to, groupBy));
    }

    // GET /api/admin/reports/popular-room-types?from=2026-01-01&to=2026-05-31&limit=5
    // Role: ADMIN, STAFF
    @GetMapping("/popular-room-types")
    public ApiResponse<List<Map<String, Object>>> getPopularRoomTypes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success("Loại phòng phổ biến", reportService.getPopularRoomTypes(from, to, limit));
    }
}
