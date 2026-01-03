package club.revived.lobby.game.kit;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * KitRoomPage
 *
 * @author yyuh
 * @since 03.01.26
 */
public record KitRoomPage(
        KitRoomPageType type,
        Map<Integer, ItemStack> content
) {

    /**
     * Creates a KitRoomPage of the given type with an empty content map.
     *
     * @param type the page type
     * @return a new KitRoomPage with the specified type and an empty content map
     */
    @NotNull
    public static KitRoomPage newEmpty(final KitRoomPageType type) {
        return new KitRoomPage(type, new HashMap<>());
    }
}