package com.tbot.notify.domain;

public record BotDeliveryResult(String botCode, boolean success, String error) {
    public static BotDeliveryResult success(String botCode) { return new BotDeliveryResult(botCode, true, null); }
    public static BotDeliveryResult failure(String botCode, String error) { return new BotDeliveryResult(botCode, false, error); }
}
