package com.tbot.notify.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RouteMessageRequest(
        @NotBlank String routeKey,
        String requestId,
        @NotNull @Valid MessagePayload message) {
}
