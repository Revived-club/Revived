package club.revived.lobby.game.item.impl;

import club.revived.commons.inventories.util.HeadBuilder;
import club.revived.commons.inventories.util.Heads;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.duel.Game;
import club.revived.lobby.game.inventory.MatchBrowserMenu;
import club.revived.lobby.game.item.ExecutableItem;
import club.revived.lobby.game.item.ExecutableItemType;
import club.revived.lobby.service.cluster.Cluster;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * MatchBrowserItem
 *
 * @author yyuh - DL
 * @since 07.01.26
 */
public final class MatchBrowserItem implements ExecutableItem {

    /**
     * Opens the match browser menu for the given player using cached games.
     *
     * @param player the player for whom the match browser will be displayed
     */
    @Override
    public void execute(final Player player) {
        Cluster.getInstance().getGlobalCache()
                .getAll("games", Game.class)
                .thenAccept(cachedDuels -> new MatchBrowserMenu(cachedDuels, player));
    }

    /**
     * Creates the Bukkit ItemStack used to represent this executable item in inventories.
     *
     * @return the ItemStack used to represent this item — a slime ball with the display name "Match Browser"
     */
    @Override
    public ItemStack toBukkitItem() {
        return ItemBuilder.item(HeadBuilder.customHead(Heads.COMPASS))
                .name("Match Browser")
                .addContainerValue("executable_item", this.id())
                .build();
    }

    /**
     * Unique identifier for this executable item.
     *
     * @return the identifier {@code "match_browser"}.
     */
    @Override
    public String id() {
        return "match_browser";
    }

    /**
     * Identifies this executable item as the match browser type.
     *
     * @return the `ExecutableItemType.MATCH_BROWSER` constant
     */
    @Override
    public ExecutableItemType type() {
        return ExecutableItemType.MATCH_BROWSER;
    }
}