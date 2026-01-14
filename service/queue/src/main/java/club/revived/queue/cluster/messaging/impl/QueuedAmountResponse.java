package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.cluster.messaging.Response;

/**
 * QueuedAmount
 *
 * @author yyuh
 * @since 14.01.26
 */
public record QueuedAmountResponse(int amount) implements Response {
}
