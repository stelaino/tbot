package com.tbot.notify.workflow;

import com.tbot.notify.config.NotifyProperties;

public record ClientPrincipal(String code, NotifyProperties.Client definition) {
}
