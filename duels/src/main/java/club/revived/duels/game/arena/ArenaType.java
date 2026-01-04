package club.revived.duels.game.arena;

import club.revived.duels.game.duels.KitType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * ArenaType
 *
 * @author yyuh
 * @since 04.01.26
 */
public enum ArenaType {

    RESTRICTED,
    INTERACTIVE

    ;

    /**
     * Provides the kit types usable for this arena type.
     *
     * @return a `List<KitType>` containing the kit types usable in this arena; may be empty
     */
    @NotNull
    public List<KitType> getUsableKits() {
        return Arrays.stream(KitType.values())
                .filter(kitType -> kitType.getArenaType() == this)
                .toList();
    }
}