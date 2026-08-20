package com.tbot.notify.workflow;

import com.tbot.notify.domain.BotDeliveryResult;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.Message;
import reactor.core.publisher.Mono;

public interface BotSender {
    boolean supports(DeliveryPlan.ResolvedBot bot);
    Mono<BotDeliveryResult> send(DeliveryPlan.ResolvedBot bot, Message message);
}
