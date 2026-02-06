package com.concert.ticketing.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    private String token;
    private String expiryDate;
}
