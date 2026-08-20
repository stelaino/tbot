package com.tbot.notify.web;

import com.tbot.notify.api.EndpointMessageRequest;
import com.tbot.notify.api.MessageResponse;
import com.tbot.notify.api.RouteMessageRequest;
import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.NotifyException;
import com.tbot.notify.workflow.ClientContext;
import com.tbot.notify.workflow.ClientPrincipal;
import com.tbot.notify.workflow.DispatchWorkflow;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
class MessageIngressController {
    private final DispatchWorkflow workflow;
    private final NotifyProperties properties;

    MessageIngressController(DispatchWorkflow workflow, NotifyProperties properties) {
        this.workflow = workflow;
        this.properties = properties;
    }

    @PostMapping("/internal/messages/route")
    Mono<MessageResponse> route(@Valid @RequestBody RouteMessageRequest request, ServerWebExchange exchange) {
        ClientPrincipal client = ClientContext.required(exchange);
        return workflow.sendRoute(client, request.routeKey(), request.requestId(), request.message());
    }

    @PostMapping("/internal/messages/endpoint/{endpointCode}")
    Mono<MessageResponse> endpoint(@PathVariable String endpointCode, @Valid @RequestBody EndpointMessageRequest request,
                                   ServerWebExchange exchange) {
        ClientPrincipal client = ClientContext.required(exchange);
        NotifyProperties.Endpoint endpoint = properties.getEndpoints().get(endpointCode);
        if (endpoint == null) throw NotifyException.notFound("ENDPOINT_NOT_FOUND", "Endpoint does not exist");
        return workflow.sendEndpoint(client, endpointCode, endpoint.getPath(), request.requestId(), request.message());
    }
}
