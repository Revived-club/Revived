package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Message;

import java.util.UUID;

/**
 * RemoveFromQueue
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public record RemoveFromQueue(UUID uuid) implements Message {
}
