package com.tbot.notify.workflow;

import com.tbot.notify.domain.NotifyException;
import org.springframework.http.HttpStatus;

final class NotifyExceptionFactory {
    private NotifyExceptionFactory() { }

    static NotifyException authFailed() {
        return new NotifyException("AUTH_FAILED", HttpStatus.UNAUTHORIZED, "API key is missing or invalid");
    }
}
