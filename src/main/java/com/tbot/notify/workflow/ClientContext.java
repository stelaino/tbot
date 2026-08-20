package com.tbot.notify.workflow;

import org.springframework.web.server.ServerWebExchange;

public final class ClientContext {
    public static final String ATTRIBUTE = ClientContext.class.getName();

    private ClientContext() { }

    public static void set(ServerWebExchange exchange, ClientPrincipal principal) {
        exchange.getAttributes().put(ATTRIBUTE, principal);
    }

    public static ClientPrincipal required(ServerWebExchange exchange) {
        ClientPrincipal principal = exchange.getAttribute(ATTRIBUTE);
        if (principal == null) {
            throw NotifyExceptionFactory.authFailed();
        }
        return principal;
    }
}
