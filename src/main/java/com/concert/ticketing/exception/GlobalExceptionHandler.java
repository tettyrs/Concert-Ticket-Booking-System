package com.concert.ticketing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if ("44".equals(ex.getCode())) {
            status = HttpStatus.NOT_FOUND;
        } else if ("40".equals(ex.getCode())) {
            status = HttpStatus.BAD_REQUEST;
        } else if ("41".equals(ex.getCode())) {
            status = HttpStatus.UNAUTHORIZED;
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "Failed");
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class).error("Unhandled exception: ", ex);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "Failed");
        response.put("code", "99");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
