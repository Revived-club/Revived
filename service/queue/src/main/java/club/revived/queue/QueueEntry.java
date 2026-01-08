package club.revived.queue;

import java.util.UUID;

/**
 * QueueEntry
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public record QueueEntry(
        UUID uuid,
        QueueType queueType,
        KitType kitType
) {}
