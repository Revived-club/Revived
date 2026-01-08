package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

public record SendActionbar(UUID uuid, String message) implements Message {
}
