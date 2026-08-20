package com.tbot.notify.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EndpointMessageRequest(String requestId, @NotNull @Valid MessagePayload message) {
}
