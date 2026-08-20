package com.tbot.notify.workflow;

import com.tbot.notify.domain.BotDeliveryResult;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.DeliveryStrategy;
import com.tbot.notify.domain.Message;
import com.tbot.notify.domain.NotifyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class DeliveryOrchestrator {
    private final List<BotSender> senders;

    public DeliveryOrchestrator(List<BotSender> senders) {
        this.senders = senders;
    }

    public Mono<List<BotDeliveryResult>> deliver(DeliveryPlan plan, Message message) {
        Flux<BotDeliveryResult> attempts = Flux.fromIterable(plan.bots())
                .concatMap(bot -> senderFor(bot).send(bot, message));
        if (plan.strategy() == DeliveryStrategy.ALL) return attempts.collectList();
        return attempts.takeUntil(BotDeliveryResult::success).collectList();
    }

    private BotSender senderFor(DeliveryPlan.ResolvedBot bot) {
        return senders.stream().filter(sender -> sender.supports(bot)).findFirst()
                .orElseThrow(() -> new NotifyException("DELIVERY_FAILED", HttpStatus.BAD_GATEWAY,
                        "No sender available for bot type " + bot.definition().getType()));
    }
}
