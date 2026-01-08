package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.KitType;
import club.revived.queue.cluster.messaging.Message;

import java.util.UUID;

/**
 * QueuePlayer
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public record QueuePlayer(
        UUID uuid,
        KitType kitType
) implements Message {
}
