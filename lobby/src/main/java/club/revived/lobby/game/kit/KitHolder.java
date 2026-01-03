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

    /**
     * Create a KitHolder populated with a full set of 14 default Kit instances.
     *
     * @param uuid the UUID to assign to the KitHolder and to each created Kit
     * @return a KitHolder containing a Map of 14 Kit objects keyed 1 through 14; each Kit is constructed with the provided UUID, an index matching its key, a name equal to the index string, an empty internal map, and a false flag
     */
    @NotNull
    public static KitHolder newEmpty(final UUID uuid) {
        final Map<Integer, Kit> kits = new HashMap<>();
        for (int i = 1; i <= 14; i++) {
            kits.put(i, new Kit(uuid, i, String.valueOf(i), new HashMap<>(), false));
        }
        return new KitHolder(uuid, kits);
    }
}