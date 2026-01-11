package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.kit.KitTemplate;
import club.revived.lobby.game.kit.PresetKitCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AdminPresetEditor
 *
 * @author yyuh
 * @since 11.01.26
 */
public final class AdminPresetEditor {

    private final AbstractMenu menu;
    private final KitType kitType;

    public AdminPresetEditor(
            final Player player,
            final KitTemplate kitTemplate
    ) {
        this.menu = AbstractMenu.of(6, String.format("Editing %s", kitTemplate.kitType().getBeautifiedName()));
        this.menu.fillEmpty(ItemBuilder.placeholder());
        this.kitType = kitTemplate.kitType();

        final var content = kitTemplate.content();

        for (int slot = 36; slot < 41; ++slot) {
            this.menu.button(new AbstractButton(slot - 36, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), _ -> {
            }));
        }

        for (int slot = 9; slot < 36; ++slot) {
            this.menu.button(new AbstractButton(slot, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), _ -> {
            }));
        }

        for (int slot = 0; slot < 9; slot++) {
            this.menu.button(new AbstractButton(slot + 36, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), _ -> {
            }));
        }

        this.menu.button(new AbstractButton(8, ItemBuilder.item(Material.CHERRY_DOOR).name("<#3B82F6>Exit")
                .lore(
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Close the menu and"),
                        ColorUtils.parse("<white>return to the main menu"),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Click to close")
                ), event -> {
            event.setCancelled(true);
            this.save();
        }));

        this.menu.onClose(_ -> {
            this.save();
        });

        this.menu.open(player);
    }

    private void save() {
        final Map<Integer, ItemStack> contents = new ConcurrentHashMap<>();
        for (int slot = 0; slot < 5; ++slot) {
            final ItemStack item = this.menu.getInventory().getItem(slot);
            contents.put(slot + 36, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
        }

        for (int slot = 9; slot < 36; ++slot) {
            final ItemStack item = this.menu.getInventory().getItem(slot);
            contents.put(slot, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
        }

        for (int slot = 36; slot < 45; ++slot) {
            final ItemStack item = this.menu.getInventory().getItem(slot);
            contents.put(slot - 36, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
        }

        final var template = new KitTemplate(
                this.kitType,
                contents
        );

        PresetKitCache.getInstance().update(template);
    }
}
