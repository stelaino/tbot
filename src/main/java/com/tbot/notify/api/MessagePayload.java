package com.tbot.notify.api;

import com.tbot.notify.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessagePayload(@NotNull MessageType type, @NotBlank String content) {
}
