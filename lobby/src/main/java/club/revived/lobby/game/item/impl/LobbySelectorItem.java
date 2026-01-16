package club.revived.lobby.game.item.impl;

import club.revived.commons.inventories.util.HeadBuilder;
import club.revived.commons.inventories.util.Heads;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.inventory.LobbySelectorMenu;
import club.revived.lobby.game.item.ExecutableItem;
import club.revived.lobby.game.item.ExecutableItemType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * LobbySelectorItem
 *
 * @author yyuh
 * @since 12.01.26
 */
public final class LobbySelectorItem implements ExecutableItem {

    @Override
    public void execute(final Player player) {
        new LobbySelectorMenu(player);
    }

    @Override
    public ItemStack toBukkitItem() {
        return ItemBuilder.item(HeadBuilder.customHead(Heads.GLOBE))
                .name("Lobby Selector")
                .addContainerValue("executable_item", this.id())
                .build();
    }

    @Override
    public String id() {
        return "lobby_selector";
    }

    @Override
    public ExecutableItemType type() {
        return ExecutableItemType.LOBBY_SELECTOR;
    }
}
