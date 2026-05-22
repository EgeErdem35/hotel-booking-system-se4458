package com.se4458.hotelbooking.hoteladminservice.common;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
