package club.revived.lobby.game.item.impl;

import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.duel.Game;
import club.revived.lobby.game.inventory.MatchBrowserMenu;
import club.revived.lobby.game.item.ExecutableItem;
import club.revived.lobby.game.item.ExecutableItemType;
import club.revived.lobby.service.cluster.Cluster;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * MatchBrowserItem
 *
 * @author yyuh - DL
 * @since 07.01.26
 */
public final class MatchBrowserItem implements ExecutableItem {

    @Override
    public void execute(final Player player) {
        Cluster.getInstance().getGlobalCache()
                .getAll("games", Game.class)
                .thenAccept(cachedDuels -> new MatchBrowserMenu(cachedDuels, player));
    }

    @Override
    public ItemStack toBukkitItem() {
        return ItemBuilder.item(Material.SLIME_BALL)
                .name("Match Browser")
                .build();
    }

    @Override
    public String id() {
        return "match_browser";
    }

    @Override
    public ExecutableItemType type() {
        return ExecutableItemType.MATCH_BROWSER;
    }
}
