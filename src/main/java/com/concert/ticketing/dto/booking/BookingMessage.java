package com.concert.ticketing.dto.booking;

public record BookingMessage(
        BookingRequest request,
        String idempotencyKey
) {
}
