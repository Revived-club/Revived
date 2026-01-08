package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.kit.Kit;
import club.revived.lobby.game.kit.KitHolder;
import club.revived.lobby.game.kit.KitRoomPageType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * KitMenu
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class KitMenu {

    private static final int[] KIT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    /**
     * Constructs and opens the "Kits" selection menu for the given player populated from the provided KitHolder.
     *
     * The menu displays each kit from the holder (left-click loads the kit; right-click opens the kit editor),
     * and includes actions for the Virtual Kit Room, Preset Kits, and Public Kits. Remaining slots are filled
     * with gray glass panes.
     *
     * @param player the player who will see and interact with the menu
     * @param kitHolder source of kits to display in the menu
     */
    public KitMenu(
            final Player player,
            final KitHolder kitHolder
    ) {
        final AbstractMenu menu = AbstractMenu.of(6, "Kits");

        for (final int i : KIT_SLOTS) {
            final int kitId = i - 9;
            final var kit = kitHolder.kits().get(kitId);

            menu.button(new AbstractButton(i, this.kitItem(kit), event -> {
                event.setCancelled(true);

                if (event.isRightClick()) {
                    new KitEditorMenu(player, kit);
                    return;
                }

                kit.load(player);
            }));
        }

        menu.button(new AbstractButton(38, ItemBuilder.item(Material.TOTEM_OF_UNDYING)
                .name(ColorUtils.parse("<#3B82F6>\uD83C\uDFA3 Virtual Kit Room"))
                .lore(
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Get items and gear in the"),
                        ColorUtils.parse("<white>kit room to create your"),
                        ColorUtils.parse("<white>custom kits."),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Click to open")
                ), event -> {
            event.setCancelled(true);
            new KitRoomMenu(player, KitRoomPageType.NETHERITE_CRYSTAL);
        }));

        menu.button(new AbstractButton(40, ItemBuilder.item(Material.NETHERITE_CHESTPLATE)
                .name(ColorUtils.parse("<#3B82F6>\uD83E\uDE93 Preset Kits"))
                .lore(
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Explore preset kits"),
                        ColorUtils.parse("<white>and use them."),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Click to open")
                ), event -> {
            event.setCancelled(true);
            // TODO: Implement opening Preset Kits
        }));

        menu.button(new AbstractButton(42, ItemBuilder.item(Material.COMPASS)
                .name(ColorUtils.parse("<#3B82F6>\uD83E\uDE93 Public Kits"))
                .lore(
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Explore other people's"),
                        ColorUtils.parse("<white>kits and use them."),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Click to open")
                ), event -> {
            event.setCancelled(true);
            // TODO: Implement opening public kits
        }));

        menu.fillEmpty(ItemBuilder.item(Material.GRAY_STAINED_GLASS_PANE)
                .tooltip(false)
                .name("")
        );

        menu.open(player);
    }

    /**
     * Create an ItemBuilder representing the given kit for display in the kit menu.
     *
     * @param kit the kit to represent; its id, name, and selected state are reflected in the resulting item
     * @return an ItemBuilder configured to display the kit's material, name, amount, and lore for the menu
     */
    @NotNull
    private ItemBuilder kitItem(final Kit kit) {
        return ItemBuilder.item(kit.selected() ? Material.KNOWLEDGE_BOOK : Material.BOOK)
                .name("<#3B82F6>\uD83C\uDFF9 Custom Kit " + kit.id())
                .amount(kit.id())
                .lore(
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Custom kits allow you to"),
                        ColorUtils.parse("<white>create preset kits which you"),
                        ColorUtils.parse("<white>can  claim at any time."),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<white>Name: <white><name>"
                                .replace("<name>", kit.name())
                        ),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>Selected: <selected>"
                                .replace("<selected>", kit.selected() ? "<green><bold>SELECTED" : "<red><bold>NOT SELECTED")
                        ),
                        ColorUtils.parse(""),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Left click to load"),
                        ColorUtils.parse("<#3B82F6>\uD83D\uDF8D● Right click to edit")
                );
    }
}