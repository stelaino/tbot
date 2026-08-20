package com.tbot.notify.workflow;

import com.tbot.notify.domain.BotDeliveryResult;
import com.tbot.notify.domain.BotType;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.Message;
import com.tbot.notify.domain.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class WeComBotSender implements BotSender {
    private static final Logger log = LoggerFactory.getLogger(WeComBotSender.class);
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public boolean supports(DeliveryPlan.ResolvedBot bot) {
        return bot.definition().getType() == BotType.WECOM;
    }

    @Override
    public Mono<BotDeliveryResult> send(DeliveryPlan.ResolvedBot bot, Message message) {
        Map<String, Object> body = message.type() == MessageType.MARKDOWN
                ? Map.of("msgtype", "markdown", "markdown", Map.of("content", message.content()))
                : Map.of("msgtype", "text", "text", Map.of("content", message.content()));
        return webClient.post()
                .uri(bot.definition().getWebhook())
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
