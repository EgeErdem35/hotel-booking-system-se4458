package com.se4458.hotelbooking.hoteladminservice.common;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
