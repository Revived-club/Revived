package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.ListMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * LobbySelectorMenu
 *
 * @author yyuh
 * @since 12.01.26
 */
public final class LobbySelectorMenu {

    private final Player player;

    public LobbySelectorMenu(final Player player) {
        this.player = player;

        ListMenu.of("Lobbies")
                .addButtons(this.lobbyItems())
                .open(player);
    }

    @NotNull
    private List<AbstractButton> lobbyItems() {
        final var itemBuilders = new ArrayList<AbstractButton>();

        for (final var entry : Cluster.getInstance().getServices().entrySet()) {
            final var service = Cluster.getInstance().getServices().get(entry.getKey());

            if (service.getType() != ServiceType.LOBBY) {
                continue;
            }

            itemBuilders.add(new AbstractButton(-1, ItemBuilder.item(Material.PLAYER_HEAD)
                    .name(String.format("<green>● %s", service.getId()))
                    .amount(Math.min(service.getOnlinePlayers().size(), 1))
                    .lore(
                            ColorUtils.parse("<gray>Click to connect")
                    ), event -> {
                event.setCancelled(true);

                final var player = PlayerManager.getInstance().fromBukkitPlayer(this.player);
                
                if (service.getId().equals(player.getCurrentServer())) {
                    player.sendMessage("<red>You are already on " + service.getId());
                    return;
                }
                
                player.connect(service.getId());
            }));
        }

        return itemBuilders;
    }
}
