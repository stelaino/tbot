package com.tbot.notify.workflow;

import com.tbot.notify.domain.NotifyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ClientRateLimiter {
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    public void check(ClientPrincipal client) {
        Instant minute = clock.instant().truncatedTo(ChronoUnit.MINUTES);
        Counter counter = counters.compute(client.code(), (key, current) ->
                current == null || !current.minute().equals(minute) ? new Counter(minute, new AtomicInteger()) : current);
        if (counter.calls().incrementAndGet() > client.definition().getRequestsPerMinute()) {
            throw new NotifyException("RATE_LIMITED", HttpStatus.TOO_MANY_REQUESTS, "Client rate limit exceeded");
        }
    }

    private record Counter(Instant minute, AtomicInteger calls) { }
}
