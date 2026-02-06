package com.concert.ticketing.dto.concert;

import com.concert.ticketing.dto.concert.ConcertRequest.TicketCategory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConcertResponse {
    private String status;
    private String code;
    private String message;
    private List<ConcertData> data;

    @Getter
    @Setter
    @Data
    public static class ConcertData {
        private UUID id;
        private String concertName;
        private String artist;
        private String venueName;
        private Integer venueCapacity;
        private String datetime;
        private String status;
        private List<TicketCategory> categories;
    }

}
