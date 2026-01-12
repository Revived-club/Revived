package club.revived.limbo.service.messaging.impl;

import club.revived.limbo.service.messaging.Message;

import java.util.UUID;

public record SendActionbar(UUID uuid, String message) implements Message {
}
