package com.concert.ticketing.controller;

import com.concert.ticketing.dto.booking.BookingResponse;
import com.concert.ticketing.dto.booking.BookingDetailDto;
import com.concert.ticketing.dto.booking.BookingRequest;
import com.concert.ticketing.services.booking.BookingService;
import com.concert.ticketing.services.kafka.BookingKafkaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Ticket booking and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
        private final BookingKafkaService kafkaService;
        private final BookingService bookingService;

        @Operation(summary = "Create booking", description = "Create a new ticket booking. Request is processed asynchronously via Kafka. Requires idempotency key to prevent duplicate bookings.")
        @PostMapping
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<BookingResponse<Void>> create(
                        @Parameter(description = "Idempotency key to prevent duplicate bookings", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @RequestHeader("X-Idempotency-Key") String idempotency,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Booking details", required = true) @RequestBody BookingRequest request)
                        throws JsonProcessingException {
                log.info("Received request to create booking. Idempotency-Key: {}", idempotency);
                kafkaService.sendToQueue(request, idempotency);

                BookingResponse<Void> response = new BookingResponse<>();
                response.setStatus("Success");
                response.setCode("00");
                response.setMessage("Booking is being processed");

                return ResponseEntity.status(202).body(response);
        }

        @Operation(summary = "Get booking details", description = "Retrieve detailed information about a specific booking")
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<BookingResponse<BookingDetailDto>> get(
                        @Parameter(description = "Booking ID", required = true) @PathVariable UUID id) {
                log.info("Received request to fetch booking detail for ID: {}", id);
                return ResponseEntity.ok(bookingService.getById(id));
        }

        @Operation(summary = "List user bookings", description = "Retrieve all bookings for a specific user")
        @GetMapping
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<BookingResponse<List<BookingDetailDto>>> list(
                        @Parameter(description = "User ID", required = true) @RequestParam UUID userId) {
                log.info("Received request to list bookings for User ID: {}", userId);
                return ResponseEntity.ok(bookingService.getUserBookings(userId));
        }

        @Operation(summary = "Cancel booking", description = "Cancel an existing booking and release reserved tickets")
        @PostMapping("/{id}/cancel")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<BookingResponse<Void>> cancelBooking(
                        @Parameter(description = "Booking ID", required = true) @PathVariable UUID id) {
                log.info("Received request to cancel booking for ID: {}", id);
                return ResponseEntity.ok(bookingService.cancel(id));
        }
}