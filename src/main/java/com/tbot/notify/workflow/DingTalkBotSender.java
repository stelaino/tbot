package com.tbot.notify.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbot.notify.domain.BotDeliveryResult;
import com.tbot.notify.domain.BotType;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.Message;
import com.tbot.notify.domain.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class DingTalkBotSender implements BotSender {
    private static final Logger log = LoggerFactory.getLogger(DingTalkBotSender.class);
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public boolean supports(DeliveryPlan.ResolvedBot bot) {
        return bot.definition().getType() == BotType.DINGTALK;
    }

    @Override
    public Mono<BotDeliveryResult> send(DeliveryPlan.ResolvedBot bot, Message message) {
        String title = bot.definition().getName() == null || bot.definition().getName().isBlank()
                ? bot.code() : bot.definition().getName();
        String content = BotMessageFormatter.format(bot.definition(), message);
        Map<String, Object> body = message.type() == MessageType.MARKDOWN
                ? Map.of("msgtype", "markdown", "markdown", Map.of("title", title, "text", content))
                : Map.of("msgtype", "text", "text", Map.of("content", content));
        String signedWebhook = DingTalkSigner.signedWebhook(bot.definition().getWebhook(),
                System.currentTimeMillis(), bot.definition().getSecret());
        return webClient.post()
                .uri(signedWebhook)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> response.path("errcode").asInt(0) == 0
                        ? BotDeliveryResult.success(bot.code())
                        : failed(bot.code()))
                .onErrorResume(error -> Mono.just(failed(bot.code())));
    }

    private BotDeliveryResult failed(String botCode) {
        log.warn("Bot delivery failed: {}", botCode);
        return BotDeliveryResult.failure(botCode, "delivery failed");
    }
}
