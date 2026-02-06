package com.concert.ticketing.controller;

import com.concert.ticketing.dto.auth.RegisterRequest;
import com.concert.ticketing.dto.auth.RegisterResponse;
import com.concert.ticketing.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "User login", description = "Authenticate user with Basic Auth (username:password in Base64) and receive JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = com.concert.ticketing.dto.auth.LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<com.concert.ticketing.dto.auth.LoginResponse> login(
            @Parameter(description = "Basic Auth header (Base64 encoded username:password)", required = true, example = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=") @RequestHeader("Authorization") String authHeader) {
        log.info("Received login request");
        if (Objects.equals(authHeader, "") || authHeader == null || !authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] credentialsDecode = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credentialsDecode, StandardCharsets.UTF_8);

        final String[] splitCrdentials = credentials.split(":", 2);
        String username = splitCrdentials[0];
        String password = splitCrdentials[1];

        return authService.login(username, password);
    }

    @Operation(summary = "Register new user", description = "Create a new user account with username, password, full name, email, and role (USER/ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful", content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration details", required = true, content = @Content(schema = @Schema(implementation = RegisterRequest.class))) @RequestBody RegisterRequest request) {
        log.info("Received registration request for username: {}", request.getUsername());
        return authService.register(request.getUsername(), request.getPassword(),
                request.getFullName(), request.getEmail(), request.getRole());
    }
}
