package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.inv.button.CyclingButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.kit.KitRoomPage;
import club.revived.lobby.game.kit.KitRoomPageType;
import club.revived.lobby.service.cluster.Cluster;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KitRoomMenu
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class KitRoomMenu {

    private final AbstractMenu menu;
    private final Map<ItemStack, KitRoomPageType> typeMap = new HashMap<>();

    private KitRoomPageType pageType;

    public KitRoomMenu(final Player player, final KitRoomPageType pageType) {
        this.menu = AbstractMenu.of(6, "Kit Room");
        this.pageType = pageType;

        // Initial stock for the Items
        this.stockItems();

        this.menu.button(CyclingButton.of(49, cycleItems(), stack -> {
            for (int i = 0; i < 45; i++) {
                this.menu.slot(i, ItemBuilder.empty(), event -> {});
            }

            this.pageType = typeMap.get(stack);
            stockItems();
        }));

        if (player.hasPermission("club.revived.edit-kitroom")) {
            this.menu.button(new AbstractButton(52, ItemBuilder.item(Material.WRITABLE_BOOK).name("<#3B82F6>Edit Page"), event -> {
                event.setCancelled(true);
                final Map<Integer, ItemStack> content = new HashMap<>();

                for (int i = 0; i < 45; i++) {
                    content.put(i, this.menu.getInventory().getItem(i));
                }

                final var page = new KitRoomPage(this.pageType, content);

                Cluster.getInstance().getGlobalCache().set(
                        this.pageType.name(),
                        page
                );

                DatabaseManager.getInstance().save(KitRoomPage.class, page);

                player.sendRichMessage("<green>Saved kitroom page!");
            }));
        }

    }

    @NotNull
    private List<ItemStack> cycleItems() {
        final var itemBuilders = new ArrayList<ItemStack>();
        final var loreLines = new ArrayList<String>();

        loreLines.add("");

        for (final KitRoomPageType pageType : KitRoomPageType.values()) {
            loreLines.add(pageType.name().toLowerCase());
        }

        loreLines.add("");
        loreLines.add("<#3B82F6>\uD83D\uDF8D● Click to Cycle");

        for (final KitRoomPageType value : KitRoomPageType.values()) {
            final var builder = ItemBuilder.item(Material.CREEPER_BANNER_PATTERN)
                    .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .name(ColorUtils.parse("<#3B82F6>Kit Room Pages"))
                    .lore(loreLines.stream()
                            .map(line -> {
                                if (value.name().toLowerCase().equals(line)) {
                                    return ColorUtils.parse("<green>▶ " + StringUtils.capitaliseAllWords(line.replace("_", " ")));
                                }

                                return ColorUtils.parse("<white>" + StringUtils.capitaliseAllWords(line.replace("_", " ")));
                            })
                            .collect(Collectors.toList())
                    )
                    .build();

            this.typeMap.put(builder, value);
            itemBuilders.add(builder);
        }

        return itemBuilders;
    }

    private void stockItems() {
        Cluster.getInstance()
                .getGlobalCache()
                .get(KitRoomPage.class, this.pageType.name())
                .thenAccept(kitRoomPage -> {
                    final var content = kitRoomPage.content();

                    for (final var i : content.keySet()) {
                        final var item = content.get(i);

                        menu.slot(i, ItemBuilder.item(item), event -> {
                        });
                    }
                });
    }
}
