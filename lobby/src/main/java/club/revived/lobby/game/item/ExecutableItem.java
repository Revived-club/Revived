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

    void execute(final Player player);
    ItemStack toBukkitItem();
    String id();
    ExecutableItemType type();
}
