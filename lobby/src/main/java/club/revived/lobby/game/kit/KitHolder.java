package club.revived.lobby.game.kit;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is an interesting Class
 *
 * @author yyuh
 * @since 03.01.26
 */
public record KitHolder(UUID uuid, Map<Integer, Kit> kits) {

    @NotNull
    public static KitHolder newEmpty(final UUID uuid) {
        final Map<Integer, Kit> kits = new HashMap<>();
        for (int i = 1; i <= 14; i++) {
            kits.put(i, new Kit(uuid, i, String.valueOf(i), new HashMap<>(), false));
        }
        return new KitHolder(uuid, kits);
    }
}
