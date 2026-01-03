package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.kit.Kit;
import club.revived.lobby.game.kit.KitHolder;
import club.revived.lobby.service.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * KitEditorMenu
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class KitEditorMenu {

    public KitEditorMenu(final Player player, final Kit kit) {
        final var menu = AbstractMenu.of(6, player.getName() + "'s Kit");
        final var networkPlayer = PlayerManager.getInstance()
                .fromBukkitPlayer(player);

        final var content = kit.content();
        for (int slot = 36; slot < 41; ++slot) {
            menu.button(new AbstractButton(slot - 36, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), event -> {
            }));
        }
        for (int slot = 9; slot < 36; ++slot) {
            menu.button(new AbstractButton(slot, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), event -> {
            }));
        }
        for (int slot = 0; slot < 9; slot++) {
            menu.button(new AbstractButton(slot + 36, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), event -> {
            }));
        }

        menu.button(new AbstractButton(8, ItemBuilder.item(Material.CHERRY_DOOR).name("<#3B82F6>Exit")
                .lore(
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Close the menu and"),
                        ColorUtils.parse("<white>return to the main menu"),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Click to close")
                ), event -> {
            event.setCancelled(true);

            networkPlayer.getCachedValue(KitHolder.class).thenAccept(kitHolder -> {
                new KitMenu(player, kitHolder);
            });
        }));
    }
}
