package club.revived.duels.game.duels;

import java.util.List;
import java.util.UUID;

/**
 * CachedDuel
 *
 * @author yyuh
 * @since 07.01.26
 */
public record GameData(
        List<UUID> blueTeam,
        List<UUID> redTeam,
        int rounds,
        KitType kitType,
        GameState gameState,
        String id
) {
}
