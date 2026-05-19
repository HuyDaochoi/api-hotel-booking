package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.common.exception.BadRequestException;
import com.yo.apihotelbooking.common.exception.NotFoundException;
import com.yo.apihotelbooking.common.util.SecurityUtils;
import com.yo.apihotelbooking.dto.request.AdminCreateUserRequest;
import com.yo.apihotelbooking.dto.request.AdminUpdateUserRequest;
import com.yo.apihotelbooking.dto.request.ChangePasswordRequest;
import com.yo.apihotelbooking.dto.request.UpdateProfileRequest;
import com.yo.apihotelbooking.dto.response.UserResponse;
import com.yo.apihotelbooking.repository.UserRepository;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // ═══════════════════════════════════════════════════════════
    // CUSTOMER — hồ sơ cá nhân
    // ═══════════════════════════════════════════════════════════

    // GET /api/users/me — xem hồ sơ của chính mình
    public UserResponse getMyProfile() {
        User user = requireCurrentUser();
        return toResponse(user);
    }

    // PUT /api/users/me — cập nhật fullName + phone
    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) throws BadRequestException {
        User user = requireCurrentUser();

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }

    // PUT /api/users/me/password — đổi mật khẩu
    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) throws BadRequestException {
        User user = requireCurrentUser();

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Không cho đặt lại mật khẩu trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN — quản lý toàn bộ user
    // ═══════════════════════════════════════════════════════════

    // GET /api/admin/users — danh sách user, filter + phân trang
    public Page<UserResponse> getAllUsers(UserRole role, Boolean isActive,
                                          String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAllWithFilter(role, isActive, keyword, pageable)
                             .map(this::toResponse);
    }

    // GET /api/admin/users/{id} — chi tiết 1 user
    public UserResponse getUserById(Long id) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));
        return toResponse(user);
    }

    // POST /api/admin/users — admin tạo user mới (CUSTOMER / STAFF / ADMIN)
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) throws BadRequestException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username đã được sử dụng: " + request.getUsername());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setIsActive(true);

        return toResponse(userRepository.save(user));
    }

    // PUT /api/admin/users/{id} — admin sửa thông tin + role + trạng thái
    @Transactional
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive());

        return toResponse(userRepository.save(user));
    }

    // PUT /api/admin/users/{id}/lock — khóa tài khoản (isActive=false)
    @Transactional
    public UserResponse lockUser(Long id) throws NotFoundException, BadRequestException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        // Không cho tự khóa tài khoản của mình
        User currentUser = requireCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("Không thể khóa tài khoản của chính mình");
        }

        user.setIsActive(false);
        return toResponse(userRepository.save(user));
    }

    // PUT /api/admin/users/{id}/unlock — mở khóa tài khoản (isActive=true)
    @Transactional
    public UserResponse unlockUser(Long id) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        user.setIsActive(true);
        return toResponse(userRepository.save(user));
    }

    // PUT /api/admin/users/{id}/reset-password — admin reset mật khẩu cho user
    @Transactional
    public void resetPassword(Long id, String newPassword) throws NotFoundException, BadRequestException {
        if (newPassword == null || newPassword.length() < 8) {
            throw new BadRequestException("Mật khẩu mới tối thiểu 8 ký tự");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ═══════════════════════════════════════════════════════════
    // STAFF — chỉ xem danh sách và chi tiết, không sửa
    // ═══════════════════════════════════════════════════════════

    // Staff dùng chung getAllUsers() và getUserById() ở trên
    // Controller sẽ phân quyền đúng cho từng endpoint

    // ═══════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════

    private User requireCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) throw new RuntimeException("Bạn chưa đăng nhập");
        return user;
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setUsername(user.getRealUser());  // getRealUser() trả username thực, không phải email
        res.setFullName(user.getFullName());
        res.setPhone(user.getPhone());
        res.setRole(user.getRole());
        res.setIsActive(user.getIsActive());
        return res;
    }
}