package club.revived.lobby.game.item;

import club.revived.lobby.Lobby;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * ExecutableItemRegistry
 *
 * @author yyuh - DL
 * @since 07.01.26
 */
public final class ExecutableItemRegistry {

    private static final Map<String, ExecutableItem> EXECUTABLE_ITEMS = new HashMap<>();

    public static void register(final ExecutableItem... items) {
        for (final ExecutableItem item : items) {
            EXECUTABLE_ITEMS.put(item.id(), item);
        }
    }

    public static ExecutableItem byType(final ExecutableItemType type) {
        return EXECUTABLE_ITEMS.values()
                .stream()
                .filter(item -> item.type() == type)
                .toList()
                .getFirst();
    }

    public static void register(final ExecutableItem item) {
        EXECUTABLE_ITEMS.put(item.id(), item);
    }

    public static boolean isExecutable(final ItemStack stack) {
        final var meta = stack.getItemMeta();

        if (meta == null) {
            return false;
        }

        final NamespacedKey key = new NamespacedKey(Lobby.getInstance(), "executable_item");

        final var data = stack.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        return data != null;
    }

    public static void execute(final Player player, final ItemStack stack) {
        final var meta = stack.getItemMeta();

        if (meta == null) {
            return;
        }

        final NamespacedKey key = new NamespacedKey(Lobby.getInstance(), "executable_item");
        final var data = stack.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (data == null) {
            return;
        }

        for (final String id : EXECUTABLE_ITEMS.keySet()) {

            if (data.equalsIgnoreCase(id)) {
                final ExecutableItem item = EXECUTABLE_ITEMS.get(id);
                if (item != null) {
                    item.execute(player);
                }
            }
        }
    }
}
