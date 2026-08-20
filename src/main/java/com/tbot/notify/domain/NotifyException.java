package com.tbot.notify.domain;

import org.springframework.http.HttpStatus;

public class NotifyException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public NotifyException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }

    public static NotifyException notFound(String code, String message) {
        return new NotifyException(code, HttpStatus.NOT_FOUND, message);
    }

    public static NotifyException forbidden(String code, String message) {
        return new NotifyException(code, HttpStatus.FORBIDDEN, message);
    }
}
