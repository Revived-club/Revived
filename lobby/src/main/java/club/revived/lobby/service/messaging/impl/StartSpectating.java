package club.revived.lobby.service.messaging.impl;

import java.util.UUID;

import club.revived.lobby.service.messaging.Message;

public record StartSpectating(
    UUID uuid,
    String duelId) implements Message {
}
