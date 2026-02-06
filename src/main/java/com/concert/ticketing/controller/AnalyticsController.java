package com.concert.ticketing.controller;

import com.concert.ticketing.dto.booking.BookingResponse;
import com.concert.ticketing.services.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and reporting endpoints for administrators")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @Operation(summary = "Get analytics dashboard (Admin only)", description = "Retrieve comprehensive analytics dashboard with sales statistics, revenue, and booking metrics")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse<Map<String, Object>>> getDashboard() {
        log.info("Received request for analytics dashboard");
        Map<String, Object> stats = analyticsService.getDashboardStats();

        BookingResponse<Map<String, Object>> response = new BookingResponse<>();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Analytics dashboard data retrieved successfully");
        response.setData(stats);

        return ResponseEntity.ok(response);
    }
}
