package com.concert.ticketing.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponse {
    private String status;
    private String code;
    private String message;
}
