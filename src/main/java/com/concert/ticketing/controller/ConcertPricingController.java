package com.concert.ticketing.controller;

import com.concert.ticketing.dto.pricing.AvailabilityItem;
import com.concert.ticketing.dto.pricing.AvailabilityResponse;
import com.concert.ticketing.dto.pricing.PricingItem;
import com.concert.ticketing.dto.pricing.PricingResponse;
import com.concert.ticketing.services.inventory.InventoryService;
import com.concert.ticketing.services.pricing.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing & Availability", description = "Real-time pricing and ticket availability endpoints")
public class ConcertPricingController {

    private final PricingService pricingService;
    private final InventoryService inventoryService;

    @Operation(summary = "Get real-time pricing", description = "Retrieve current dynamic pricing for all ticket categories of a concert")
    @GetMapping("/{id}/pricing")
    public ResponseEntity<PricingResponse> getConcertPricing(
            @Parameter(description = "Concert ID", required = true) @PathVariable UUID id) {
        log.info("Received request for real-time pricing for concert ID: {}", id);
        List<PricingItem> pricing = pricingService.getRealTimePricing(id);

        PricingResponse response = new PricingResponse();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Pricing retrieved successfully");
        response.setData(pricing);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get real-time availability", description = "Retrieve current ticket availability for all categories of a concert")
    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> getConcertAvailability(
            @Parameter(description = "Concert ID", required = true) @PathVariable UUID id) {
        log.info("Received request for real-time availability for concert ID: {}", id);
        List<AvailabilityItem> availability = inventoryService.getAvailability(id);

        AvailabilityResponse response = new AvailabilityResponse();
        response.setStatus("Success");
        response.setCode("00");
        response.setMessage("Availability retrieved successfully");
        response.setData(availability);

        return ResponseEntity.ok(response);
    }
}
