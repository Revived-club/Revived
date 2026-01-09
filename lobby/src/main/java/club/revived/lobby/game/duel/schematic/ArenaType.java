package club.revived.lobby.game.duel.schematic;

import club.revived.lobby.game.WarpLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
    public static ArenaType fromId(final String id) {
        return Arrays.stream(ArenaType.values())
                .filter(arenaType -> arenaType.name().equals(id))
                .toList()
                .getFirst();
    }

    @NotNull
    public static String[] toStringArray() {
        return Arrays.stream(ArenaType.values())
                .map(ArenaType::name)
                .toArray(String[]::new);
    }
}