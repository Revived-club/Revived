package club.revived.lobby.service.messaging.impl;


import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.messaging.Message;

import java.util.List;
import java.util.UUID;

public record FFAEnd(
        UUID winner,
        List<UUID> participants,
        KitType kitType,
        long elapsedTime
) implements Message {
}
