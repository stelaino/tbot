package com.tbot.notify.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbot.notify.config.NotifyProperties;
import com.tbot.notify.domain.DeliveryPlan;
import com.tbot.notify.domain.Message;
import com.tbot.notify.domain.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeishuBotSenderTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendsTextMessagesUsingTheFeishuTextPayload() {
        var body = FeishuBotSender.requestBody(bot(), new Message(MessageType.TEXT, "hello"));

        var json = objectMapper.valueToTree(body);

        assertEquals("text", json.path("msg_type").asText());
        assertEquals("【通知】hello", json.path("content").path("text").asText());
    }

    @Test
    void sendsMarkdownMessagesUsingAnInteractiveCard() {
        var body = FeishuBotSender.requestBody(bot(), new Message(MessageType.MARKDOWN, "# report"));

        var json = objectMapper.valueToTree(body);

        assertEquals("interactive", json.path("msg_type").asText());
        assertEquals("lark_md", json.path("card").path("elements").get(0).path("text").path("tag").asText());
        assertEquals("【通知】# report", json.path("card").path("elements").get(0).path("text").path("content").asText());
    }

    @Test
    void acceptsOnlyFeishuSuccessResponses() throws Exception {
        assertTrue(FeishuBotSender.isSuccessful(objectMapper.readTree("{\"code\":0}")));
        assertFalse(FeishuBotSender.isSuccessful(objectMapper.readTree("{\"code\":19022}")));
        assertFalse(FeishuBotSender.isSuccessful(objectMapper.readTree("{}")));
    }

    private static DeliveryPlan.ResolvedBot bot() {
        var definition = new NotifyProperties.Bot();
        definition.setHeader("【通知】");
        return new DeliveryPlan.ResolvedBot("feishu-bot", definition);
    }
}
