package club.revived.duels.game.duels;

import club.revived.commons.generic.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Duel
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Duel {

    private final List<UUID> blueTeam;
    private final List<UUID> redTeam;
    private final int rounds;
    private final KitType kitType;
    private final String id;

    private GameState gameState = GameState.PREPARING;

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
        this.id = StringUtils.generateId("#game-");
    }

    public List<Player> getBluePlayers() {
        return this.blueTeam
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Player> getRedPlayers() {
        return this.redTeam
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Player> getPlayers() {
        // TODO: Replace, there's probably a better way to do this
        final var uuids = new ArrayList<>(this.redTeam);
        uuids.addAll(this.blueTeam);

        return uuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
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

    public String getId() {
        return id;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
