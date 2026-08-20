package com.tbot.notify.workflow;

import com.tbot.notify.api.MessagePayload;
import com.tbot.notify.api.MessageResponse;
import com.tbot.notify.domain.BotDeliveryResult;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.DeliveryStatus;
import com.tbot.notify.domain.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class DispatchWorkflow {
    private final ApiKeyAuthenticator authenticator;
    private final IdempotencyGuard idempotencyGuard;
    private final TargetResolver resolver;
    private final DeliveryOrchestrator orchestrator;

    public DispatchWorkflow(ApiKeyAuthenticator authenticator, IdempotencyGuard idempotencyGuard,
                            TargetResolver resolver, DeliveryOrchestrator orchestrator) {
        this.authenticator = authenticator;
        this.idempotencyGuard = idempotencyGuard;
        this.resolver = resolver;
        this.orchestrator = orchestrator;
    }

    public Mono<MessageResponse> sendRoute(ClientPrincipal client, String routeKey, String requestId, MessagePayload payload) {
        authenticator.authorizeRoute(client, routeKey);
        return dispatch(client, requestId, payload, resolver.route(routeKey));
    }

    public Mono<MessageResponse> sendEndpoint(ClientPrincipal client, String endpointCode, String endpointPath,
                                              String requestId, MessagePayload payload) {
        authenticator.authorizeEndpoint(client, endpointCode);
        return dispatch(client, requestId, payload, resolver.endpoint(endpointCode, endpointPath));
    }

    private Mono<MessageResponse> dispatch(ClientPrincipal client, String requestId, MessagePayload payload, DeliveryPlan plan) {
        String effectiveRequestId = requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
        idempotencyGuard.acquire(client.code(), effectiveRequestId);
        return orchestrator.deliver(plan, new Message(payload.type(), payload.content()))
                .map(results -> response(effectiveRequestId, results));
    }

    private MessageResponse response(String requestId, List<BotDeliveryResult> results) {
        int success = (int) results.stream().filter(BotDeliveryResult::success).count();
        int failed = results.size() - success;
        DeliveryStatus status = success == 0 ? DeliveryStatus.FAILED : failed == 0 ? DeliveryStatus.SUCCESS : DeliveryStatus.PARTIAL_SUCCESS;
        return new MessageResponse(requestId, status, results.size(), success, failed);
    }
}
