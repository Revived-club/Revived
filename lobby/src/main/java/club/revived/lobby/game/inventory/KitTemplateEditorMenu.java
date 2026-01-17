package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.kit.EditedKitHolder;
import club.revived.lobby.game.kit.EditedKitTemplate;
import club.revived.lobby.game.kit.KitTemplate;
import club.revived.lobby.service.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KitTemplateEditorMenu
 *
 * @author yyuh
 * @since 16.01.26
 */
public final class KitTemplateEditorMenu {

  public KitTemplateEditorMenu(
      final EditedKitTemplate template,
      final Player player) {
    final var menu = AbstractMenu.of(6, "Editing " + template.kitType().getBeautifiedName());
    final var networkPlayer = PlayerManager.getInstance()
        .fromBukkitPlayer(player);

    final var content = template.content();
    for (int slot = 36; slot < 41; ++slot) {
      menu.button(new AbstractButton(slot - 36,
          ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), event -> {
          }));
    }
    for (int slot = 9; slot < 36; ++slot) {
      menu.button(
          new AbstractButton(slot, ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), event -> {
          }));
    }
    for (int slot = 0; slot < 9; slot++) {
      menu.button(new AbstractButton(slot + 36,
          ItemBuilder.item(content.getOrDefault(slot, new ItemStack(Material.AIR))), event -> {
          }));
    }

    menu.onClose(event -> {
      networkPlayer.getCachedOrLoad(EditedKitHolder.class).thenAccept(editedKitHolder -> {
        final Map<Integer, ItemStack> contents = new ConcurrentHashMap<>();

        for (int slot = 0; slot < 5; slot++) {
          final ItemStack item = event.getInventory().getItem(slot);
          contents.put(slot + 36, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
        }

        for (int slot = 9; slot < 36; slot++) {
          final ItemStack item = event.getInventory().getItem(slot);
          contents.put(slot, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
        }

        for (int slot = 36; slot < 45; slot++) {
          final ItemStack item = event.getInventory().getItem(slot);
          contents.put(slot - 36, Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.AIR)));
        }

        final var kit = new EditedKitTemplate(
            player.getUniqueId(),
            template.kitType(),
            contents);

        editedKitHolder.kits().put(template.kitType(), kit);

        networkPlayer.sendMessage("Your kit is being saved");
        networkPlayer.cacheValue(EditedKitHolder.class, editedKitHolder);
        DatabaseManager.getInstance().save(EditedKitHolder.class, editedKitHolder);
      });
    });

    menu.open(player);
  }
}
