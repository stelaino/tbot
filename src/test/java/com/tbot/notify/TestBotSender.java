package com.tbot.notify;

import com.tbot.notify.domain.BotDeliveryResult;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.Message;
import com.tbot.notify.workflow.BotSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("test")
class TestBotSender implements BotSender {
    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public boolean supports(DeliveryPlan.ResolvedBot bot) {
        return true;
    }

    @Override
    public Mono<BotDeliveryResult> send(DeliveryPlan.ResolvedBot bot, Message message) {
        calls.incrementAndGet();
        return Mono.just("dev-bot".equals(bot.code())
                ? BotDeliveryResult.failure(bot.code(), "simulated failure")
                : BotDeliveryResult.success(bot.code()));
    }

    int calls() { return calls.get(); }
}
