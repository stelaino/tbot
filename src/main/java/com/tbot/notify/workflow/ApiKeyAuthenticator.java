package com.tbot.notify.workflow;

import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.NotifyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Component
public class ApiKeyAuthenticator {
    private final NotifyProperties properties;

    public ApiKeyAuthenticator(NotifyProperties properties) {
        this.properties = properties;
    }

    public ClientPrincipal authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) throw NotifyExceptionFactory.authFailed();
        for (Map.Entry<String, NotifyProperties.Client> entry : properties.getClients().entrySet()) {
            NotifyProperties.Client client = entry.getValue();
            if (client.isEnabled() && MessageDigest.isEqual(
                    client.getApiKey().getBytes(StandardCharsets.UTF_8), apiKey.getBytes(StandardCharsets.UTF_8))) {
                return new ClientPrincipal(entry.getKey(), client);
            }
        }
        throw NotifyExceptionFactory.authFailed();
    }

    public void authorizeRoute(ClientPrincipal client, String routeKey) {
        if (!client.definition().getAllowedRoutes().contains(routeKey)) {
            throw NotifyException.forbidden("CLIENT_INVALID", "Client is not allowed to use route " + routeKey);
        }
    }

    public void authorizeEndpoint(ClientPrincipal client, String endpointCode) {
        if (!client.definition().getAllowedEndpoints().contains(endpointCode)) {
            throw NotifyException.forbidden("CLIENT_INVALID", "Client is not allowed to use endpoint " + endpointCode);
        }
    }
}
