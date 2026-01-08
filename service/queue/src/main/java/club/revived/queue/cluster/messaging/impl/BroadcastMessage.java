package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Message;

/**
 * BroadcastMessage
 *
 * @author yyuh
 * @since 06.01.26
 */
public record BroadcastMessage(String message) implements Message {
}
