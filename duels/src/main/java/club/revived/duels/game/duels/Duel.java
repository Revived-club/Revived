package club.revived.duels.game.duels;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Duel
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Duel {

    public enum TeamType {
        RED,
        BLUE
    }

    public record Team(
            List<Player> players,
            TeamType type
    ) {

        @Override
        public List<Player> players() {
            return this.players;
        }

        public boolean containsPlayer(final Player player) {
            return this.players.contains(player);
        }
    }

    private final List<UUID> blueTeam;
    private final List<UUID> redTeam;
    private final int rounds;
    private final KitType kitType;



    public Duel(
            final List<UUID> blueTeam,
            final List<UUID> redTeam,
            final int rounds,
            final KitType kitType
    ) {
        this.blueTeam = blueTeam;
        this.redTeam = redTeam;
        this.rounds = rounds;
        this.kitType = kitType;
    }

    public List<UUID> getBlueTeam() {
        return blueTeam;
    }

    public List<UUID> getRedTeam() {
        return redTeam;
    }

    public int getRounds() {
        return rounds;
    }

    public KitType getKitType() {
        return kitType;
    }
}
