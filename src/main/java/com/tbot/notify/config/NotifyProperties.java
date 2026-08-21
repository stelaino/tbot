package com.tbot.notify.config;

import com.tbot.notify.domain.BotType;
import com.tbot.notify.domain.DeliveryStrategy;
import com.tbot.notify.domain.TargetType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("notify")
public class NotifyProperties {
    private Map<String, Client> clients = new LinkedHashMap<>();
    private Map<String, Bot> bots = new LinkedHashMap<>();
    private Map<String, Group> groups = new LinkedHashMap<>();
    private Map<String, Route> routes = new LinkedHashMap<>();
    private Map<String, Endpoint> endpoints = new LinkedHashMap<>();
    private Duration duplicateTtl = Duration.ofMinutes(10);

    public Map<String, Client> getClients() { return clients; }
    public void setClients(Map<String, Client> clients) { this.clients = clients; }
    public Map<String, Bot> getBots() { return bots; }
    public void setBots(Map<String, Bot> bots) { this.bots = bots; }
    public Map<String, Group> getGroups() { return groups; }
    public void setGroups(Map<String, Group> groups) { this.groups = groups; }
    public Map<String, Route> getRoutes() { return routes; }
    public void setRoutes(Map<String, Route> routes) { this.routes = routes; }
    public Map<String, Endpoint> getEndpoints() { return endpoints; }
    public void setEndpoints(Map<String, Endpoint> endpoints) { this.endpoints = endpoints; }
    public Duration getDuplicateTtl() { return duplicateTtl; }
    public void setDuplicateTtl(Duration duplicateTtl) { this.duplicateTtl = duplicateTtl; }

    public static class Client {
        private String apiKey;
        private boolean enabled = true;
        private int requestsPerMinute = 60;
        private List<String> allowedRoutes = List.of();
        private List<String> allowedEndpoints = List.of();
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
        public List<String> getAllowedRoutes() { return allowedRoutes; }
        public void setAllowedRoutes(List<String> allowedRoutes) { this.allowedRoutes = allowedRoutes; }
        public List<String> getAllowedEndpoints() { return allowedEndpoints; }
        public void setAllowedEndpoints(List<String> allowedEndpoints) { this.allowedEndpoints = allowedEndpoints; }
    }

    public static class Bot {
        private String name;
        private BotType type;
        private String webhook;
        private String secret;
        private String header;
        private boolean enabled = true;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BotType getType() { return type; }
        public void setType(BotType type) { this.type = type; }
        public String getWebhook() { return webhook; }
        public void setWebhook(String webhook) { this.webhook = webhook; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public String getHeader() { return header; }
        public void setHeader(String header) { this.header = header; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Group {
        private List<String> bots = List.of();
        public List<String> getBots() { return bots; }
        public void setBots(List<String> bots) { this.bots = bots; }
    }

    public static class Route {
        private TargetType targetType;
        private String target;
        private DeliveryStrategy strategy = DeliveryStrategy.ALL;
        private boolean enabled = true;
        public TargetType getTargetType() { return targetType; }
        public void setTargetType(TargetType targetType) { this.targetType = targetType; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public DeliveryStrategy getStrategy() { return strategy; }
        public void setStrategy(DeliveryStrategy strategy) { this.strategy = strategy; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Endpoint {
        private String path;
        private TargetType targetType;
        private String target;
        private DeliveryStrategy strategy = DeliveryStrategy.ALL;
        private boolean enabled = true;
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public TargetType getTargetType() { return targetType; }
        public void setTargetType(TargetType targetType) { this.targetType = targetType; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public DeliveryStrategy getStrategy() { return strategy; }
        public void setStrategy(DeliveryStrategy strategy) { this.strategy = strategy; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
