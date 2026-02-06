package com.concert.ticketing.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse<T> {
    private String status;
    private String code;
    private String message;
    private T data;
}
