package club.revived.queue.cluster.messaging.impl;

import club.revived.duels.game.duels.KitType;
import club.revived.queue.cluster.messaging.Message;

import java.util.List;
import java.util.UUID;

public record MigrateGame(
        List<UUID> blueTeam,
        List<UUID> redTeam,
        int maxRounds,
        KitType kitType,
        int redScore,
        int blueScore,
        String arenaId,
        String gameServerId
) implements Message {
}
