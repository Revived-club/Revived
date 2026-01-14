package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.KitType;
import club.revived.queue.QueueType;
import club.revived.queue.cluster.messaging.Request;

/**
 * QueuedAmount
 *
 * @author yyuh
 * @since 14.01.26
 */
public record QueuedAmountRequest(
        KitType kitType,
        QueueType queueType
) implements Request {
}
