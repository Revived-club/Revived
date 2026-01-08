package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Message;

import java.util.UUID;

public record SendActionbar(UUID uuid, String message) implements Message {
}
