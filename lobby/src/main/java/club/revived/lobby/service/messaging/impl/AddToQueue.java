package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.messaging.Message;

import java.util.UUID;

/**
 * QueuePlayer
 *
 * @author yyuh - DL
 * @since 1/8/26
 */
public record AddToQueue(
        UUID uuid,
        KitType kitType
) implements Message {
}
