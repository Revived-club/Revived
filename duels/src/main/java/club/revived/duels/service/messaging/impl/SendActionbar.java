package club.revived.duels.service.messaging.impl;

import club.revived.duels.service.messaging.Message;

import java.util.UUID;

public record SendActionbar(UUID uuid, String message) implements Message {
}
