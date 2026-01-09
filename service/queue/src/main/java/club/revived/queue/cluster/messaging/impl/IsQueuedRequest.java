package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Request;

import java.util.UUID;

/**
 * IsQueuedRequest
 *
 * @author yyuh
 * @since 09.01.26
 */
public record IsQueuedRequest(UUID uuid) implements Request {
}
