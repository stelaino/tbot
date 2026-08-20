package com.tbot.notify.workflow;

import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.DeliveryStrategy;
import com.tbot.notify.domain.NotifyException;
import com.tbot.notify.domain.TargetType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TargetResolver {
    private final NotifyProperties properties;

    public TargetResolver(NotifyProperties properties) {
        this.properties = properties;
    }

    public DeliveryPlan route(String routeKey) {
        NotifyProperties.Route route = properties.getRoutes().get(routeKey);
        if (route == null) throw NotifyException.notFound("ROUTE_NOT_FOUND", "Route does not exist: " + routeKey);
        if (!route.isEnabled()) throw new NotifyException("ROUTE_DISABLED", HttpStatus.CONFLICT, "Route is disabled: " + routeKey);
        return resolve(route.getTargetType(), route.getTarget(), route.getStrategy());
    }

    public DeliveryPlan endpoint(String endpointCode, String path) {
        NotifyProperties.Endpoint endpoint = properties.getEndpoints().get(endpointCode);
        if (endpoint == null || !path.equals(endpoint.getPath())) {
            throw NotifyException.notFound("ENDPOINT_NOT_FOUND", "Endpoint does not exist");
        }
        if (!endpoint.isEnabled()) throw new NotifyException("ENDPOINT_DISABLED", HttpStatus.CONFLICT, "Endpoint is disabled: " + endpointCode);
        return resolve(endpoint.getTargetType(), endpoint.getTarget(), endpoint.getStrategy());
    }

    private DeliveryPlan resolve(TargetType type, String target, DeliveryStrategy strategy) {
        return switch (type) {
            case BOT -> new DeliveryPlan(strategy, List.of(bot(target)));
            case GROUP -> new DeliveryPlan(strategy, group(target));
            case ROUTE -> route(target);
        };
    }

    private List<DeliveryPlan.ResolvedBot> group(String groupCode) {
        NotifyProperties.Group group = properties.getGroups().get(groupCode);
        if (group == null) throw NotifyException.notFound("GROUP_NOT_FOUND", "Group does not exist: " + groupCode);
        return group.getBots().stream().map(this::bot).toList();
    }

    private DeliveryPlan.ResolvedBot bot(String botCode) {
        NotifyProperties.Bot bot = properties.getBots().get(botCode);
        if (bot == null) throw NotifyException.notFound("BOT_NOT_FOUND", "Bot does not exist: " + botCode);
        if (!bot.isEnabled()) throw new NotifyException("BOT_DISABLED", HttpStatus.CONFLICT, "Bot is disabled: " + botCode);
        return new DeliveryPlan.ResolvedBot(botCode, bot);
    }
}
