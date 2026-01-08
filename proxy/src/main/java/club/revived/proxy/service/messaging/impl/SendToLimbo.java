package club.revived.proxy.service.messaging.impl;

import club.revived.proxy.service.messaging.Message;

import java.util.UUID;

public record SendToLimbo(UUID uuid) implements Message {
}
