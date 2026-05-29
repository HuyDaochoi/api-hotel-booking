package com.yo.apihotelbooking.services.jwt;

import com.yo.apihotelbooking.schemas.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.yo.apihotelbooking.schemas.domain.Role;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    // ── Tạo Access Token ────────────────────────────────────────────
    public String generateAccessToken(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("roles", roleNames)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSignKey())
                .compact();
    }


    public String generateRefreshToken(User user) {
       return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignKey())
                .compact();
    }
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

 public boolean isTokenValid(String token, UserDetails userDetails) {
        if (userDetails instanceof User user) {
            final String userId = extractUserId(token);
            return userId.equals(String.valueOf(user.getId())) && !isTokenExpired(token);
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
public String extractJti(String token) {
    return extractClaim(token, Claims::getId);
}
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}