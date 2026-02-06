package com.concert.ticketing.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingItem {
    private UUID categoryId;
    private String concertName;
    private String artistName;
    private BigDecimal currentPrice;
    private Integer availableStock;
}
