package com.concert.ticketing.exception;

import com.concert.ticketing.constant.ErrorList;
import com.concert.ticketing.constant.Origin;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final Origin origin;
    private final String code;

    public ServiceException(Origin origin, ErrorList errorList) {
        super(errorList.getDescription());
        this.origin = origin;
        this.code = errorList.getCode();
    }
}
