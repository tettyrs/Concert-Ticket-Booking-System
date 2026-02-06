package com.concert.ticketing.utils;

import com.concert.ticketing.dto.auth.TokenResponse;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {
    private final SecretKey key;

    public JwtUtils() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        log.info("JwtUtils initialized with a new SecretKey");
    }

    private static final long expire = 1000 * 60 * 15; // 15 menit

    public TokenResponse generateToken(String username) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + expire);

        LocalDateTime localExpiry = LocalDateTime.now().plusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedExpiry = localExpiry.format(formatter);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();

        return TokenResponse.builder()
                .token(token)
                .expiryDate(formattedExpiry)
                .build();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT Validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
