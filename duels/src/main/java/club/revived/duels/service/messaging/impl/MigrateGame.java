package club.revived.duels.service.messaging.impl;

import club.revived.duels.game.duels.KitType;
import club.revived.duels.service.messaging.Message;

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
