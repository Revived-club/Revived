package club.revived.duels.game.duels;

import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.message.DuelStart;

import java.util.List;
import java.util.UUID;

/**
 * DuelManager
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DuelManager {

    public DuelManager() {
        Cluster.getInstance().getMessagingService()
                .registerMessageHandler(DuelStart.class, duelStart -> {

                });
    }

    private void startDuel(
            final List<UUID> blueTeam,
            final List<UUID> redTeam,
            final int rounds,
            final KitType kitType
    ) {

    }
}
