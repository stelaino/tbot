package com.tbot.notify.workflow;

import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.Message;

final class BotMessageFormatter {
    private BotMessageFormatter() { }

    static String format(NotifyProperties.Bot bot, Message message) {
        String header = bot.getHeader();
        return header == null || header.isBlank() ? message.content() : header + message.content();
    }
}
