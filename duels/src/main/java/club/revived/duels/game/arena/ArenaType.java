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

    @NotNull
    public List<KitType> getUsableKits() {
        return Arrays.stream(KitType.values())
                .filter(kitType -> kitType.getArenaType() == this)
                .toList();
    }
}