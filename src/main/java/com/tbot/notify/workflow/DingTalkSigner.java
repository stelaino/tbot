package com.tbot.notify.workflow;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class DingTalkSigner {
    private DingTalkSigner() { }

    static String signedWebhook(String webhook, long timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            String separator = webhook.contains("?") ? "&" : "?";
            return webhook + separator + "timestamp=" + timestamp + "&sign="
                    + URLEncoder.encode(signature, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign DingTalk webhook", exception);
        }
    }
}
