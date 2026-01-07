package club.revived.lobby.game.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * ExecutableItem
 *
 * @author yyuh - DL
 * @since 07.01.26
 */
public interface ExecutableItem {

    /**
 * Performs this item's action for the given player.
 *
 * @param player the player who triggered or used the item
 */
void execute(final Player player);
    /**
 * Provides a Bukkit ItemStack that represents this executable item.
 *
 * @return the Bukkit ItemStack representing this executable item
 */
ItemStack toBukkitItem();
    /**
 * Unique identifier for this executable item.
 *
 * @return the identifier string for this executable item
 */
String id();
    /**
 * Gets the executable item's type.
 *
 * @return the {@link ExecutableItemType} that categorizes this executable item
 */
ExecutableItemType type();
}