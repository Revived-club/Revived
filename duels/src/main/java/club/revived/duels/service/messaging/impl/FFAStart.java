package club.revived.duels.service.messaging.impl;

import club.revived.duels.game.duels.KitType;

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
) {
}
