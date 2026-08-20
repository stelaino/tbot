package com.tbot.notify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MessageIngressIntegrationTest {
    @Autowired
    private WebTestClient client;

    @Test
    void chatGptActionUsesDirectPostAndReturnsPartialSuccess() {
        client.post().uri("/api/v1/messages")
                .header("X-Api-Key", "chatgpt-test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(routeRequest("chatgpt-notify", "chatgpt-1"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PARTIAL_SUCCESS")
                .jsonPath("$.total").isEqualTo(2)
                .jsonPath("$.successCount").isEqualTo(1)
                .jsonPath("$.failedCount").isEqualTo(1);
    }

    @Test
    void chatGptActionCanSendToTheGossipGroup() {
        client.post().uri("/api/v1/messages")
                .header("X-Api-Key", "chatgpt-test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(routeRequest("gossip-notify", "gossip-1"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("SUCCESS")
                .jsonPath("$.total").isEqualTo(1)
                .jsonPath("$.successCount").isEqualTo(1);
    }

    @Test
    void endpointCanTargetOneBot() {
        client.post().uri("/hook/finance")
                .header("X-Api-Key", "finance-test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("requestId", "finance-1", "message", message()))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("SUCCESS");
    }

    @Test
    void routeCanTargetOneBot() {
        client.post().uri("/api/v1/messages")
                .header("X-Api-Key", "finance-test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(routeRequest("finance-message", "finance-route-1"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("SUCCESS");
    }

    @Test
    void fixedEndpointCanResolveRouteAndGroup() {
        client.post().uri("/hook/production-alert")
                .header("X-Api-Key", "monitoring-test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("requestId", "production-hook-1", "message", message()))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("PARTIAL_SUCCESS");
    }

    @Test
    void firstSuccessAndPrimaryBackupStopAfterFirstSuccessfulBot() {
        client.post().uri("/api/v1/messages").header("X-Api-Key", "monitoring-test-key")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(routeRequest("first-success", "first-1"))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.status").isEqualTo("SUCCESS").jsonPath("$.total").isEqualTo(1);
        client.post().uri("/api/v1/messages").header("X-Api-Key", "monitoring-test-key")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(routeRequest("primary-backup", "backup-1"))
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.status").isEqualTo("SUCCESS").jsonPath("$.total").isEqualTo(1);
    }

    @Test
    void duplicateRequestIsRejectedBeforeDelivery() {
        Map<String, Object> request = routeRequest("production-alert", "duplicate-1");
        client.post().uri("/api/v1/messages").header("X-Api-Key", "monitoring-test-key")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isOk();
        client.post().uri("/api/v1/messages").header("X-Api-Key", "monitoring-test-key")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("DUPLICATE_REQUEST");
    }

    @Test
    void unconfiguredEndpointIsRejected() {
        client.post().uri("/hook/not-exist")
                .header("X-Api-Key", "finance-test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("message", message()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("ENDPOINT_NOT_FOUND");
    }

    private static Map<String, Object> routeRequest(String routeKey, String requestId) {
        return Map.of("routeKey", routeKey, "requestId", requestId, "message", message());
    }

    private static Map<String, Object> message() {
        return Map.of("type", "TEXT", "content", "test notification");
    }
}
