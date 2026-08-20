package com.tbot.notify.api;

import com.tbot.notify.domain.DeliveryStatus;

public record MessageResponse(String requestId, DeliveryStatus status, int total, int successCount, int failedCount) {
}
