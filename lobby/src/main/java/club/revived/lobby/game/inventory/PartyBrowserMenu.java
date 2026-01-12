package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.ListMenu;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.parties.Party;
import club.revived.lobby.service.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * PartyBrowserMenu
 *
 * @author yyuh
 * @since 12.01.26
 */
public final class PartyBrowserMenu {

    public PartyBrowserMenu(
            final List<Party> parties,
            final Player player
    ) {
        ListMenu.of("Party Browser")
                .addItems(this.partyItems(parties))
                .open(player);
    }

    @NotNull
    private List<ItemBuilder> partyItems(final List<Party> parties) {
        final var itemBuilders = new ArrayList<ItemBuilder>();

        for (final var party : parties) {

            itemBuilders.add(ItemBuilder.item(Material.MAGMA_CREAM)
                    .name(String.format("<white>%s's Party", PlayerManager.getInstance().isRegistered(party.getOwner()) ?
                            PlayerManager.getInstance().fromBukkitPlayer(party.getOwner()).getUsername() :
                            "Unknown"
                    ))
                    .lore(
                            ColorUtils.parse(String.format("Members %d", party.getMembers().size()))
                    )
            );
        }

        return itemBuilders;
    }
}
