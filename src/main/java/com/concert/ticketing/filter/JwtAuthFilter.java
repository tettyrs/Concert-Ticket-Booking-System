package com.concert.ticketing.filter;

import com.concert.ticketing.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    public JwtAuthFilter(JwtUtils jwtUtil,
            org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.startsWith("/api/v1/auth/")) {

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.debug("Found Bearer token, validating...");
        if (jwtUtil.isTokenValid(token)) {

            String username = jwtUtil.getUsername(token);
            log.debug("Token valid for user: {}", username);

            String sessionKey = "session::" + username + "::" + token;
            Object role = redisTemplate.opsForValue().get(sessionKey);

            if (role == null) {
                log.warn("Session not found in Redis for user: {}. Token might be expired or logged out.", username);
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("Session found in Redis. Role raw value: {}", role);

            // Bersihkan quotes jika ada (karena Jackson serializer)
            String roleStr = role.toString().replace("\"", "");

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleStr));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities);

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            log.info("Authentication set in SecurityContext for user: {}", username);
        } else {
            log.warn("Invalid JWT token detected");
        }

        filterChain.doFilter(request, response);
    }

}