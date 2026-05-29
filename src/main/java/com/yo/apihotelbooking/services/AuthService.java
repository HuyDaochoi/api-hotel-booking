package com.yo.apihotelbooking.services;

import com.yo.apihotelbooking.dto.request.LoginRequest;
import com.yo.apihotelbooking.dto.request.RegisterRequest;
import com.yo.apihotelbooking.dto.response.AuthResponse;
import com.yo.apihotelbooking.schemas.domain.RefreshToken;
import com.yo.apihotelbooking.schemas.domain.User;
import com.yo.apihotelbooking.schemas.domain.Role;
import com.yo.apihotelbooking.repository.RefreshTokenRepository;
import com.yo.apihotelbooking.repository.RoleRepository;
import com.yo.apihotelbooking.repository.UserRepository;
import com.yo.apihotelbooking.services.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("USERNAME_ALREADY_EXISTS");
        }

      User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(true);

       Role defaultRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("LỖI: Không tìm thấy quyền dữ liệu CUSTOMER trong hệ thống DB"));
        user.getRoles().add(defaultRole);

        userRepository.save(user);

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        refreshTokenRepository.revokeAllByUserId(user.getId());

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {

        RefreshToken savedToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("REFRESH_TOKEN_NOT_FOUND"));

        if (savedToken.isRevoked()) {
            throw new RuntimeException("REFRESH_TOKEN_REVOKED");
        }

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("REFRESH_TOKEN_EXPIRED");
        }

        User user = savedToken.getUser();

        savedToken.setRevoked(true);
        refreshTokenRepository.save(savedToken);

        return createAuthResponse(user);
    }

@Transactional
    public void logout(String refreshTokenValue) {

        RefreshToken savedToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("REFRESH_TOKEN_NOT_FOUND"));

        savedToken.setRevoked(true);
        refreshTokenRepository.save(savedToken);
    }

private AuthResponse createAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = saveRefreshToken(user);
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName) 
                .collect(Collectors.toSet());
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setRoles(roleNames);

        return response;
    }


    private String saveRefreshToken(User user) {
        String tokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenValue);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}