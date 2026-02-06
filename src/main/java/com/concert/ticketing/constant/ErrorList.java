package com.concert.ticketing.constant;

import lombok.Getter;

@Getter
public enum ErrorList {
    FAILED_CONNECT_TO_BACKEND("40", "Failed connect to backend"),
    DATA_NOT_FOUND("44", "Data not found"),
    USER_NOT_FOUND("44", "User not found"),
    INVALID_CREDENTIALS("41", "Invalid username or password");

    private final String code;
    private final String description;

    ErrorList(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
