package club.revived.lobby.game.duel;

import java.util.UUID;

/**
 * Request to fight another player.
 *
 * @author yyuh
 * @since 03.01.26
 */
public record DuelRequest(
        UUID sender,
        UUID receiver,
        int rounds,
        KitType kitType
) {
}
