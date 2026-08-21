package com.tbot.notify.config;

import com.tbot.notify.domain.TargetType;
import com.tbot.notify.domain.BotType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
class NotifyConfigurationValidator {
    private final NotifyProperties properties;

    NotifyConfigurationValidator(NotifyProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void validate() {
        require(!properties.getClients().isEmpty(), "notify.clients must not be empty");
        require(!properties.getBots().isEmpty(), "notify.bots must not be empty");
        properties.getClients().forEach((code, client) -> {
            require(hasText(client.getApiKey()), "client " + code + " must define api-key");
            require(client.getRequestsPerMinute() > 0, "client " + code + " rate limit must be positive");
        });
        properties.getBots().forEach((code, bot) -> {
            require(bot.getType() != null, "bot " + code + " must define type");
            require(hasText(bot.getWebhook()), "bot " + code + " must define webhook");
            if (bot.getType() == BotType.DINGTALK) {
                require(hasText(bot.getSecret()), "DingTalk bot " + code + " must define secret");
            }
        });
        properties.getGroups().forEach((code, group) -> {
            require(!group.getBots().isEmpty(), "group " + code + " must contain bots");
            group.getBots().forEach(bot -> require(properties.getBots().containsKey(bot), "group " + code + " references missing bot " + bot));
        });
        properties.getRoutes().forEach((code, route) -> validateTarget("route " + code, route.getTargetType(), route.getTarget(), false));
        Set<String> paths = new HashSet<>();
        properties.getEndpoints().forEach((code, endpoint) -> {
            require(hasText(endpoint.getPath()) && endpoint.getPath().startsWith("/hook/"),
                    "endpoint " + code + " path must start with /hook/");
            require(paths.add(endpoint.getPath()), "endpoint paths must be unique: " + endpoint.getPath());
            validateTarget("endpoint " + code, endpoint.getTargetType(), endpoint.getTarget(), true);
        });
    }

    private void validateTarget(String source, TargetType type, String target, boolean allowRoute) {
        require(type != null && hasText(target), source + " must define target-type and target");
        if (type == TargetType.BOT) require(properties.getBots().containsKey(target), source + " references missing bot " + target);
        if (type == TargetType.GROUP) require(properties.getGroups().containsKey(target), source + " references missing group " + target);
        if (type == TargetType.ROUTE) {
            require(allowRoute, source + " cannot target a route");
            require(properties.getRoutes().containsKey(target), source + " references missing route " + target);
        }
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new BeanCreationException(message);
    }
}
