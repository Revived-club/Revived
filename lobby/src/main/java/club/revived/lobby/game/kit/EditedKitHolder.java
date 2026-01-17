package club.revived.lobby.game.kit;

import club.revived.lobby.game.duel.KitType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * EditedKitHolder
 *
 * @author yyuh
 * @since 17.01.26
 */
public record EditedKitHolder(
        UUID uuid,
        Map<KitType, EditedKitTemplate> kits
) {

    @NotNull
    public static EditedKitHolder newEmpty(final UUID uuid) {
        final var kits = new HashMap<KitType, EditedKitTemplate>();

        for (final var kitType : KitType.values()) {
            final var kit = PresetKitCache.getInstance().get(kitType);
            kits.put(kitType, EditedKitTemplate.fromTemplate(uuid, kit));
        }

        return new EditedKitHolder(
                uuid,
                kits
        );
    }
}
