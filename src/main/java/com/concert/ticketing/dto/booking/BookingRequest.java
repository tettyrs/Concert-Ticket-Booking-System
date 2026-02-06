package com.concert.ticketing.dto.booking;

import lombok.Getter;

import java.util.UUID;

public record BookingRequest(
        UUID userId,
        UUID eventId,
        UUID categoryId,
        Integer quantity
) {
}
