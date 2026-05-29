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
import com.yo.apihotelbooking.repository.RoleRepository;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository  roleRepository; 

    public UserResponse getMyProfile() {
        User user = requireCurrentUser();
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) throws BadRequestException {
        User user = requireCurrentUser();

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) throws BadRequestException {
        User user = requireCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public Page<UserResponse> getAllUsers(String role, Boolean isActive,
                                          String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return userRepository.findAllWithFilter(role, isActive, keyword, pageable)
                             .map(this::toResponse);
    }

    public UserResponse getUserById(Long id) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));
        return toResponse(user);
    }

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
        user.setIsActive(true);

        if (request.getRole() != null) {
            Role targetRole = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy quyền: " + request.getRole()));
            user.getRoles().add(targetRole);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(request.getIsActive());

        if (request.getRole() != null) {
            Role targetRole = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy quyền: " + request.getRole()));
            user.getRoles().clear();
            user.getRoles().add(targetRole);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse lockUser(Long id) throws NotFoundException, BadRequestException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        User currentUser = requireCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("Không thể khóa tài khoản của chính mình");
        }

        user.setIsActive(false);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse unlockUser(Long id) throws NotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng: " + id));

        user.setIsActive(true);
        return toResponse(userRepository.save(user));
    }

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

    private User requireCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) throw new RuntimeException("Bạn chưa đăng nhập");
        return user;
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setUsername(user.getRealUser());  
        res.setFullName(user.getFullName());
        res.setPhone(user.getPhone());
        res.setIsActive(user.getIsActive());
        
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            res.setRole(user.getRoles().iterator().next().getName());
        }
        
        return res;
    }
}