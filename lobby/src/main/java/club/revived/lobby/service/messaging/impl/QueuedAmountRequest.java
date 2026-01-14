package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.duel.QueueType;
import club.revived.lobby.service.messaging.Request;

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
