package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.messaging.Message;

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
