package com.concert.ticketing.dto.concert;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcertRequest {
    private String name;
    private String artist;
    private String venue;
    private ZonedDateTime datetime;
    private String capacity;
    private Integer totalCapacity;
    private String status;
    private String timezone;
    private List<TicketCategory> categories;
    private String description;

    @Data
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TicketCategory{
        private String categoryName;
        private BigDecimal basePrice;
        private Integer totalAllocation;
    }
}
