package com.tbot.notify.web;

import com.tbot.notify.api.ErrorResponse;
import com.tbot.notify.domain.NotifyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotifyException.class)
    ResponseEntity<ErrorResponse> notifyError(NotifyException error) {
        return ResponseEntity.status(error.status()).body(new ErrorResponse(error.code(), error.getMessage(), null));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ErrorResponse> validationError() {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST", "Request body is invalid", null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpectedError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Unexpected server error", null));
    }
}
