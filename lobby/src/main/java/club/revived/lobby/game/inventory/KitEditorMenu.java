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

    /**
     * Constructs a 6-row inventory menu for editing the given player's kit and populates it with the kit's contents.
     *
     * The menu is filled with items from the provided kit and includes an Exit button that cancels the interaction
     * and opens the main Kit menu for the player's KitHolder when clicked.
     *
     * @param player the player for whom the kit editor menu is created
     * @param kit    the kit whose contents populate the menu
     */
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