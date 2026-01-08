package club.revived.queue.cluster.messaging.impl;

import club.revived.duels.game.duels.KitType;
import club.revived.queue.cluster.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * DuelEnd
 *
 * @author yyuh - DL
 * @since 1/7/26
 */
public record DuelEnd(
        List<UUID> winner,
        List<UUID> loser,
        int maxScore,
        int winnerScore,
        int loserScore,
        KitType kitType,
        long elapsedTime
) implements Message {
}
