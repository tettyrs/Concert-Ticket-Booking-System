package com.concert.ticketing.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingResponse {
    private String status;
    private String code;
    private String message;
    private List<PricingItem> data;
}
