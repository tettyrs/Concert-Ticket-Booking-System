package com.concert.ticketing.controller;

import com.concert.ticketing.dto.booking.BookingResponse;
import com.concert.ticketing.services.ledger.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Settlement & Transactions", description = "Financial settlement and transaction history endpoints for administrators")
@SecurityRequirement(name = "bearerAuth")
public class SettlementController {
    private final SettlementService settlementService;

    @Operation(summary = "Get concert settlement report (Admin only)", description = "Retrieve detailed financial settlement report for a specific concert including revenue, refunds, and net settlement")
    @GetMapping("/concerts/{id}/settlement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse<Map<String, Object>>> getSettlementReport(
            @Parameter(description = "Concert ID", required = true) @PathVariable UUID id) {
        log.info("Received request for settlement report for concert ID: {}", id);
        Map<String, Object> report = settlementService.getSettlementReport(id);

        BookingResponse<Map<String, Object>> response = new BookingResponse<>();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Settlement report retrieved successfully");
        response.setData(report);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all transactions (Admin only)", description = "Retrieve complete transaction history across all concerts and bookings")
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse<Object>> getAllTransactions() {
        log.info("Received request to list all transactions");
        Object transactions = settlementService.getAllTransactions();

        BookingResponse<Object> response = new BookingResponse<>();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("All transactions retrieved successfully");
        response.setData(transactions);

        return ResponseEntity.ok(response);
    }
}
