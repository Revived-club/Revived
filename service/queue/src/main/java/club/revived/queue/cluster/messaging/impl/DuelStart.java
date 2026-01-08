package club.revived.queue.cluster.messaging.impl;

import club.revived.queue.KitType;
import club.revived.queue.cluster.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record DuelStart(
        List<UUID> blueTeam,
        List<UUID> redTeam,
        int rounds,
        KitType kitType
) implements Message {
}
