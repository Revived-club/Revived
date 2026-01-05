package club.revived.duels.service.messaging.impl;

import club.revived.duels.game.duels.KitType;
import club.revived.duels.service.messaging.Message;

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
