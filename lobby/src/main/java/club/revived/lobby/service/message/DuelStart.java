package club.revived.lobby.service.message;

import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.messaging.Message;

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
