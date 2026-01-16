package club.revived.lobby.game.item.impl;

import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.inventory.PartyBrowserMenu;
import club.revived.lobby.game.item.ExecutableItem;
import club.revived.lobby.game.item.ExecutableItemType;
import club.revived.lobby.game.parties.Party;
import club.revived.lobby.service.cluster.Cluster;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * PartyBrowserItem
 *
 * @author yyuh
 * @since 12.01.26
 */
public final class PartyBrowserItem implements ExecutableItem {

    @Override
    public void execute(final Player player) {
        Cluster.getInstance().getGlobalCache()
                .getAll("parties", Party.class)
                .thenAccept(parties -> {
                    player.sendMessage(parties.toString());
                    new PartyBrowserMenu(
                            parties.stream().filter(party -> party.isOpen() && !party.isDisbanded()).toList(),
                            player
                    );
                });
    }

    @Override
    public ItemStack toBukkitItem() {
        return ItemBuilder.item(Material.BLAZE_POWDER)
                .name("Party Browser")
                .addContainerValue("executable_item", this.id())
                .build();
    }

    @Override
    public String id() {
        return "party_browser";
    }

    @Override
    public ExecutableItemType type() {
        return ExecutableItemType.PARTY_BROWSER;
    }
}
