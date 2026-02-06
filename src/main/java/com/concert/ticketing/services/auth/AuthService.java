package com.concert.ticketing.services.auth;

import com.concert.ticketing.dto.auth.LoginResponse;
import com.concert.ticketing.dto.auth.RegisterResponse;
import com.concert.ticketing.dto.auth.TokenResponse;
import com.concert.ticketing.model.UsersModel;
import com.concert.ticketing.repositories.UserRepository;
import com.concert.ticketing.constant.ErrorList;
import com.concert.ticketing.constant.Origin;
import com.concert.ticketing.exception.ServiceException;
import com.concert.ticketing.utils.JwtUtils;
import java.time.Duration;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private Gson gson = new Gson();

    public ResponseEntity<LoginResponse> login(String username, String password) {
        try {
            log.info("Starting login process for user: {}", username);
            Long startTime = System.currentTimeMillis();
            LoginResponse response = new LoginResponse();

            UsersModel user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Login failed: User '{}' not found", username);
                        return new ServiceException(Origin.POSTGRE, ErrorList.USER_NOT_FOUND);
                    });

            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Login failed: Invalid password for user '{}'", username);
                throw new ServiceException(Origin.POSTGRE, ErrorList.INVALID_CREDENTIALS);
            }
            TokenResponse token = jwtUtils.generateToken(username);

            response.setStatus("Success");
            response.setCode("00");
            response.setMessage("OK");
            response.setToken(token.getToken());
            response.setExpiryDate(token.getExpiryDate());

            // Simpan session ke Redis: session::username::token -> role
            String sessionKey = "session::" + username + "::" + token.getToken();
            redisTemplate.opsForValue().set(sessionKey, user.getRole(), Duration.ofMinutes(15));
            log.info("Session stored in Redis for user: {} with key suffix: ...{}", username,
                    token.getToken().substring(token.getToken().length() - 8));

            Long duration = System.currentTimeMillis() - startTime;
            log.info("Login successful for user: {}. Duration: {} ms", username, duration);
            log.debug("Full Login response: {}", gson.toJson(response));

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ServiceException e) {
            log.error("ServiceException during login: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for user '{}': {}", username, e.getMessage(), e);
            throw new ServiceException(Origin.TOKEN, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }
    }

    public ResponseEntity<RegisterResponse> register(String username, String password,
            String fullName, String email, String role) {
        try {
            log.info("Starting registration for user: {}, role: {}", username, role);
            Long startTime = System.currentTimeMillis();
            RegisterResponse registerResponse = new RegisterResponse();
            String hashedPassword = passwordEncoder.encode(password);

            String finalRole = role.toUpperCase();
            if (finalRole.equals("ADMIN") || finalRole.equals("USER")) {
                UsersModel newUser = new UsersModel(username, hashedPassword, fullName, email, finalRole);
                userRepository.save(newUser);
                log.info("User '{}' successfully saved to database with role '{}'", username, finalRole);
            } else {
                log.warn("Registration attempt with invalid role: {}", role);
            }
            registerResponse.setStatus("Success");
            registerResponse.setCode("00");
            registerResponse.setMessage("OK");

            Long duration = System.currentTimeMillis() - startTime;
            log.info("Registration completed for user: {}. Duration: {} ms", username, duration);
            log.debug("Full Register response: {}", gson.toJson(registerResponse));

            return ResponseEntity.status(HttpStatus.OK).body(registerResponse);
        } catch (Exception e) {
            log.error("Unexpected error during registration for user '{}': {}", username, e.getMessage(), e);
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }
    }
}
