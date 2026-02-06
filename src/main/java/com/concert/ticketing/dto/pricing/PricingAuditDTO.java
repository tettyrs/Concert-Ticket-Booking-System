package com.concert.ticketing.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

public class PricingAuditDTO {
    private UUID categoryId;
    private BigDecimal priceAt;
    private Integer stockAt;
    private Long timestamp;
    private String traceId;

    public PricingAuditDTO(UUID categoryId, BigDecimal currentPrice, Integer availableStock) {
    }
}
