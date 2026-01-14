package club.revived.duels.service.messaging.impl;

import club.revived.duels.game.duels.KitType;
import club.revived.duels.service.messaging.Message;

import java.util.List;
import java.util.UUID;

public record FFAEnd(
        UUID winner,
        List<UUID> participants,
        KitType kitType,
        long elapsedTime
) implements Message {
}
