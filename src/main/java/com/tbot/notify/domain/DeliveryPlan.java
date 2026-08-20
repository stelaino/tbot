package com.tbot.notify.domain;

import com.tbot.notify.config.NotifyProperties;

import java.util.List;

public record DeliveryPlan(DeliveryStrategy strategy, List<ResolvedBot> bots) {
    public record ResolvedBot(String code, NotifyProperties.Bot definition) { }
}
