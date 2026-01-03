package club.revived.lobby.game.duel;

import java.util.List;
import java.util.UUID;

/**
 * DuelRequestHolder
 *
 * @author yyuh
 * @since 03.01.26
 */
public record DuelRequestHolder(UUID uuid, List<DuelRequest> duelRequests) {
}
