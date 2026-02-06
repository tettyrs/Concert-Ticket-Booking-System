package com.concert.ticketing.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "Booking ID is required")
        UUID bookingId,

        @NotBlank(message = "Payment method is required")
        String method,

        @NotBlank(message = "Gateway Transaction ID is required")
        String gatewayTransactionId,

        @NotNull(message = "Amount must be provided")
        BigDecimal amount,

        String currency
) {}
