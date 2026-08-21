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
import java.util.List;
import java.util.Map;

@Component
public class FeishuBotSender implements BotSender {
    private static final Logger log = LoggerFactory.getLogger(FeishuBotSender.class);
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public boolean supports(DeliveryPlan.ResolvedBot bot) {
        return bot.definition().getType() == BotType.FEISHU;
    }

    @Override
    public Mono<BotDeliveryResult> send(DeliveryPlan.ResolvedBot bot, Message message) {
        return webClient.post()
                .uri(bot.definition().getWebhook())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody(bot, message))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> isSuccessful(response)
                        ? BotDeliveryResult.success(bot.code())
                        : failed(bot.code()))
                .onErrorResume(error -> Mono.just(failed(bot.code())));
    }

    static Map<String, Object> requestBody(DeliveryPlan.ResolvedBot bot, Message message) {
        String content = BotMessageFormatter.format(bot.definition(), message);
        if (message.type() == MessageType.MARKDOWN) {
            return Map.of(
                    "msg_type", "interactive",
                    "card", Map.of(
                            "elements", List.of(Map.of(
                                    "tag", "div",
                                    "text", Map.of("tag", "lark_md", "content", content)))));
        }
        return Map.of("msg_type", "text", "content", Map.of("text", content));
    }

    static boolean isSuccessful(JsonNode response) {
        return response.path("code").asInt(-1) == 0;
    }

    private BotDeliveryResult failed(String botCode) {
        log.warn("Bot delivery failed: {}", botCode);
        return BotDeliveryResult.failure(botCode, "delivery failed");
    }
}
