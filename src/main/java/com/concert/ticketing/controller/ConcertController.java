package com.concert.ticketing.controller;

import com.concert.ticketing.dto.concert.ConcertRequest;
import com.concert.ticketing.dto.concert.ConcertResponse;
import com.concert.ticketing.services.concert.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
@Tag(name = "Concerts", description = "Concert management and information endpoints")
public class ConcertController {

    private final ConcertService concertService;

    @Operation(summary = "List/search concerts", description = "Get all concerts with optional filtering by name, artist, venue, date range, and minimum capacity")
    @GetMapping
    public ResponseEntity<ConcertResponse> getAllConcerts(
            @Parameter(description = "Filter by concert name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by artist name") @RequestParam(required = false) String artist,
            @Parameter(description = "Filter by venue name") @RequestParam(required = false) String venue,
            @Parameter(description = "Filter by start date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @Parameter(description = "Filter by end date (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @Parameter(description = "Filter by minimum capacity") @RequestParam(required = false) Integer minCapacity) {
        log.info("Received request to list concerts with filters. MinCapacity: {}", minCapacity);
        ConcertResponse response = concertService.getAllConcerts(name, artist, venue, startDate, endDate, minCapacity);

        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Concerts retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get concert details", description = "Retrieve detailed information about a specific concert by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConcertResponse> getConcertById(
            @Parameter(description = "Concert ID", required = true) @PathVariable UUID id) {
        log.info("Received request to fetch concert detail for ID: {}", id);
        ConcertResponse response = concertService.getConcertDetail(id);

        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Concert detail retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create concert (Admin only)", description = "Create a new concert with ticket categories. Requires ADMIN role.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConcertResponse> createConcert(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Concert details with ticket categories", required = true) @RequestBody ConcertRequest request) {
        log.info("Received request to create concert: {}", request.getName());
        concertService.createConcert(request);

        ConcertResponse response = new ConcertResponse();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Concert and Ticket Categories created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update concert (Admin only)", description = "Update an existing concert. Requires ADMIN role.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConcertResponse> updateConcert(
            @Parameter(description = "Concert ID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated concert details", required = true) @RequestBody ConcertRequest request) {
        log.info("Received request to update concert ID: {}", id);
        concertService.updateConcert(id, request);

        ConcertResponse response = new ConcertResponse();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Concert updated successfully");
        return ResponseEntity.ok(response);
    }
}
