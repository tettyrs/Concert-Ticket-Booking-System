package com.concert.ticketing.dto.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityItem {
    @JsonProperty("categoryId")
    private UUID categoryId;
    @JsonProperty("categoryName")
    private String categoryName;
    @JsonProperty("concertName")
    private String concertName;
    @JsonProperty("artistName")
    private String artistName;
    @JsonProperty("totalAllocation")
    private Integer totalAllocation;
    @JsonProperty("availableStock")
    private Integer availableStock;
    @JsonProperty("status")
    private String status;
}
