package com.tbot.notify.workflow;

import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.NotifyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyGuard {
    private final ConcurrentHashMap<String, Instant> seen = new ConcurrentHashMap<>();
    private final NotifyProperties properties;
    private final Clock clock = Clock.systemUTC();

    public IdempotencyGuard(NotifyProperties properties) {
        this.properties = properties;
    }

    public void acquire(String clientCode, String requestId) {
        if (requestId == null || requestId.isBlank()) return;
        Instant now = clock.instant();
        Instant expiry = now.plus(properties.getDuplicateTtl());
        String key = clientCode + ':' + requestId;
        boolean[] acquired = {false};
        seen.compute(key, (ignored, oldExpiry) -> {
            if (oldExpiry == null || oldExpiry.isBefore(now)) {
                acquired[0] = true;
                return expiry;
            }
            return oldExpiry;
        });
        if (!acquired[0]) {
            throw new NotifyException("DUPLICATE_REQUEST", HttpStatus.CONFLICT, "Request was already processed");
        }
    }
}
