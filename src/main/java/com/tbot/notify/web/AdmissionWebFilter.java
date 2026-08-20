package com.tbot.notify.web;

import com.tbot.notify.workflow.ApiKeyAuthenticator;
import com.tbot.notify.workflow.ClientContext;
import com.tbot.notify.workflow.ClientPrincipal;
import com.tbot.notify.workflow.ClientRateLimiter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdmissionWebFilter implements WebFilter {
    private final ApiKeyAuthenticator authenticator;
    private final ClientRateLimiter rateLimiter;

    public AdmissionWebFilter(ApiKeyAuthenticator authenticator, ClientRateLimiter rateLimiter) {
        this.authenticator = authenticator;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.equals("/api/v1/messages") && !path.startsWith("/hook/")) return chain.filter(exchange);
        ClientPrincipal client = authenticator.authenticate(exchange.getRequest().getHeaders().getFirst("X-Api-Key"));
        rateLimiter.check(client);
        ClientContext.set(exchange, client);
        return chain.filter(exchange);
    }
}
