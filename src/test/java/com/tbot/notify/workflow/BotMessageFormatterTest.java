package com.tbot.notify.workflow;

import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.Message;
import com.tbot.notify.domain.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BotMessageFormatterTest {
    private final Message message = new Message(MessageType.TEXT, "服务已启动");

    @Test
    void prependsTheOptionalPerBotHeader() {
        NotifyProperties.Bot bot = new NotifyProperties.Bot();
        bot.setHeader("【统一消息转发中心】");

        assertEquals("【统一消息转发中心】服务已启动", BotMessageFormatter.format(bot, message));
    }

    @Test
    void keepsTheOriginalContentWhenHeaderIsMissingOrBlank() {
        NotifyProperties.Bot bot = new NotifyProperties.Bot();
        assertEquals("服务已启动", BotMessageFormatter.format(bot, message));
        bot.setHeader("  ");
        assertEquals("服务已启动", BotMessageFormatter.format(bot, message));
    }
}
