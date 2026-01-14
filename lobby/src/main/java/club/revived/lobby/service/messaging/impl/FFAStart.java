package club.revived.lobby.service.messaging.impl;

import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * StartFFA
 *
 * @author yyuh
 * @since 14.01.26
 */
public record FFAStart(
        List<UUID> players,
        KitType kitType
) implements Message {
}
