package club.revived.duels.service.messaging.impl;

import java.util.UUID;

import club.revived.duels.service.messaging.Message;

public record StartSpectating(
    UUID uuid,
    String duelId) implements Message {
}
