package com.tbot.notify.web;

import com.tbot.notify.config.NotifyProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
class GatewayRouteConfiguration {
    private final NotifyProperties properties;

    GatewayRouteConfiguration(NotifyProperties properties) {
        this.properties = properties;
    }

    @Bean
    RouteLocator messageRoutes(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes()
                .route("standard-message-api", route -> route.path("/api/v1/messages").and().method(HttpMethod.POST)
                        .uri("forward:/internal/messages/route"));
        properties.getEndpoints().forEach((code, endpoint) -> routes.route("fixed-message-endpoint-" + code,
                route -> route.path(endpoint.getPath()).and().method(HttpMethod.POST)
                        .uri("forward:/internal/messages/endpoint/" + code)));
        return routes.route("unknown-message-endpoint", route -> route.path("/hook/**").and().method(HttpMethod.POST)
                        .uri("forward:/internal/messages/endpoint/not-found"))
                .build();
    }
}
