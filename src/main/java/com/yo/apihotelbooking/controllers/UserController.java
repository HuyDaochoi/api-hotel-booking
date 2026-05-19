package com.yo.apihotelbooking.controllers;

import com.yo.apihotelbooking.common.ApiResponse;
import com.yo.apihotelbooking.dto.request.AdminCreateUserRequest;
import com.yo.apihotelbooking.dto.request.AdminUpdateUserRequest;
import com.yo.apihotelbooking.dto.request.ChangePasswordRequest;
import com.yo.apihotelbooking.dto.request.UpdateProfileRequest;
import com.yo.apihotelbooking.dto.response.UserResponse;
import com.yo.apihotelbooking.schemas.enums.UserRole;
import com.yo.apihotelbooking.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ═══════════════════════════════════════════════════════════
    // CUSTOMER — hồ sơ cá nhân (tất cả role đã login đều dùng được)
    // Base: /api/users/me
    // ═══════════════════════════════════════════════════════════

    // GET /api/users/me — xem hồ sơ của chính mình
    @GetMapping("/api/users/me")
    public ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.success(userService.getMyProfile());
    }

    // PUT /api/users/me — cập nhật fullName + phone
    @PutMapping("/api/users/me")
    public ApiResponse<UserResponse> updateMyProfile(
            @RequestBody @Valid UpdateProfileRequest request) throws Exception {
        return ApiResponse.success("Cập nhật hồ sơ thành công",
                userService.updateMyProfile(request));
    }

    // PUT /api/users/me/password — đổi mật khẩu
    @PutMapping("/api/users/me/password")
    public ApiResponse<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) throws Exception {
        userService.changeMyPassword(request);
        return ApiResponse.successMessage("Đổi mật khẩu thành công");
    }

    // ═══════════════════════════════════════════════════════════
    // STAFF — chỉ xem danh sách + chi tiết user, KHÔNG sửa
    // Base: /api/admin/users  (dùng chung path admin nhưng STAFF đọc được)
    // ═══════════════════════════════════════════════════════════

    // GET /api/admin/users — danh sách user (filter + phân trang)
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.getAllUsers(role, isActive, keyword, page, size));
    }

    // GET /api/admin/users/{id} — chi tiết 1 user
    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) throws Exception {
        return ApiResponse.success(userService.getUserById(id));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN — toàn quyền quản lý user
    // ═══════════════════════════════════════════════════════════

    // POST /api/admin/users — tạo user mới (CUSTOMER / STAFF / ADMIN)
    @PostMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(
            @RequestBody @Valid AdminCreateUserRequest request) throws Exception {
        return ApiResponse.success("Tạo người dùng thành công",
                userService.createUser(request));
    }

    // PUT /api/admin/users/{id} — sửa thông tin + role + trạng thái
    @PutMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AdminUpdateUserRequest request) throws Exception {
        return ApiResponse.success("Cập nhật người dùng thành công",
                userService.updateUser(id, request));
    }

    // PUT /api/admin/users/{id}/lock — khóa tài khoản
    @PutMapping("/api/admin/users/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> lockUser(@PathVariable Long id) throws Exception {
        return ApiResponse.success("Đã khóa tài khoản",
                userService.lockUser(id));
    }

    // PUT /api/admin/users/{id}/unlock — mở khóa tài khoản
    @PutMapping("/api/admin/users/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> unlockUser(@PathVariable Long id) throws Exception {
        return ApiResponse.success("Đã mở khóa tài khoản",
                userService.unlockUser(id));
    }

    // PUT /api/admin/users/{id}/reset-password — reset mật khẩu
    // Body: { "newPassword": "..." }
    @PutMapping("/api/admin/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) throws Exception {
        String newPassword = body.get("newPassword");
        userService.resetPassword(id, newPassword);
        return ApiResponse.successMessage("Reset mật khẩu thành công");
    }
}