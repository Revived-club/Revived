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

    /**
     * Constructs a Duel for the specified blue and red teams with the given number of rounds and kit type, and assigns a generated unique identifier.
     *
     * @param blueTeam list of UUIDs for blue team members
     * @param redTeam list of UUIDs for red team members
     * @param rounds the number of rounds for the duel
     * @param kitType the kit configuration used in the duel
     */
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

    /**
     * Get the online Player objects for members of the blue team.
     *
     * @return a list of Player objects for blue team members who are currently online; an empty list if none are online
     */
    public List<Player> getBluePlayers() {
        return this.blueTeam
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Get online Player objects for members of the red team.
     *
     * Converts stored red-team UUIDs to Player instances and excludes members who are not currently online.
     *
     * @return a list of online players who are members of the red team; offline or unknown players are omitted
     */
    public List<Player> getRedPlayers() {
        return this.redTeam
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Retrieves all online Player instances who are members of either team in this duel.
     *
     * @return a list of players from both teams; any team members who are offline are excluded
     */
    public List<Player> getPlayers() {
        // TODO: Replace, there's probably a better way to do this
        final var uuids = new ArrayList<>(this.redTeam);
        uuids.addAll(this.blueTeam);

        return uuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }


    /**
     * Returns the UUIDs of players assigned to the blue team.
     *
     * @return a list of UUIDs representing the blue team members
     */
    public List<UUID> getBlueTeam() {
        return blueTeam;
    }

    /**
     * Retrieve the red team's member UUIDs.
     *
     * @return the list of UUIDs representing players on the red team
     */
    public List<UUID> getRedTeam() {
        return redTeam;
    }

    /**
     * Returns the number of rounds configured for this duel.
     *
     * @return the number of rounds for the duel
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * The kit configuration used for this duel.
     *
     * @return the KitType for this duel
     */
    public KitType getKitType() {
        return kitType;
    }

    /**
     * Retrieves the unique identifier for this duel.
     *
     * @return the duel's generated identifier string
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current game state of the duel.
     *
     * @return the current {@link GameState} representing the duel's state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Set the duel's current game state.
     *
     * @param gameState the new state for this duel
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}