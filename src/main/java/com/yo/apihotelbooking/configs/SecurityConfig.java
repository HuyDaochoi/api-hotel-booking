package com.yo.apihotelbooking.configs;

import com.yo.apihotelbooking.services.jwt.CustomUserDetailsService;
import com.yo.apihotelbooking.services.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService; 

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC: không cần token ──────────────────────────────
                .requestMatchers(
                    "/api/auth/**",
                    "/api/rooms/available",
                    "/api/room-types/**",
                    "/api/pricing/estimate",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // ── STAFF + ADMIN: đặt TRƯỚC rule /api/admin/** ──────────
                // Vì Spring đọc từ trên xuống, rule cụ thể phải đứng trước rule tổng quát
                .requestMatchers(
                    "/api/admin/bookings/*/check-in",
                    "/api/admin/bookings/*/check-out",
                    "/api/admin/bookings/*/confirm",
                    "/api/admin/bookings/*/no-show"
                ).hasAnyRole("STAFF", "ADMIN")

                // ── ADMIN only ────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Còn lại phải đăng nhập ───────────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService); 
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}