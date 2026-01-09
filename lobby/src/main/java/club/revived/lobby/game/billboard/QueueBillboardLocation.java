package club.revived.lobby.game.billboard;

import club.revived.lobby.game.duel.KitType;
import org.bukkit.Location;

/**
 * QueueBillboardLocation
 *
 * @author yyuh
 * @since 09.01.26
 */
public record QueueBillboardLocation(
        KitType kitType,
        Location location
) {
}