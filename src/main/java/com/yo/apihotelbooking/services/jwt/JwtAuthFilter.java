package com.yo.apihotelbooking.services.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Đọc header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Không có token hoặc sai format → cho qua, SecurityConfig sẽ chặn nếu cần
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Tách lấy JWT (bỏ "Bearer ")
        final String jwt = authHeader.substring(7);

        try {
            // 4. Extract email từ token
            final String email = jwtService.extractEmail(jwt);

            // 5. Chỉ xử lý nếu chưa có authentication trong context
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user từ DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 7. Validate token (email khớp + còn hạn)
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 8. Tạo Authentication và set vào SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // Token hết hạn → trả 401, client cần dùng refresh token
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_EXPIRED", "Token đã hết hạn, vui lòng đăng nhập lại");

        } catch (JwtException e) {
            // Token giả mạo hoặc bị sửa → trả 401
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_INVALID", "Token không hợp lệ");
        }
    }

    // ── Helper: viết JSON error response ────────────────────────────
    private void sendErrorResponse(
            HttpServletResponse response,
            int status,
            String errorCode,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\": false, \"errorCode\": \"" + errorCode + "\", " +
                "\"message\": \"" + message + "\"}"
        );
    }
}