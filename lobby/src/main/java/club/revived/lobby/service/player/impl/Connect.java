package club.revived.lobby.service.player.impl;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

public record Connect(UUID uuid, String server) implements Message {
}
