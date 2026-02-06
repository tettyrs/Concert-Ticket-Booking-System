package com.concert.ticketing.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailDto {
        private UUID id;
        private String eventName;
        private String categoryName;
        private Integer quantity;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime expiresAt;
        private String idempotencyKey;
}
