package com.tbot.notify.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DingTalkSignerTest {
    @Test
    void appendsTheDocumentedHmacSignature() {
        String webhook = "https://oapi.dingtalk.com/robot/send?access_token=token";

        String actual = DingTalkSigner.signedWebhook(webhook, 1_700_000_000_000L, "SECtest");

        assertEquals(webhook + "&timestamp=1700000000000&sign=aZLLrriXgn05YbwaGR7knYsLeJADjr9NwLaNNKpxh4g%3D", actual);
    }
}
