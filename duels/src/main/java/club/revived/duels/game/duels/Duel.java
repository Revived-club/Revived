package club.revived.duels.game.duels;

import club.revived.commons.generic.StringUtils;
import club.revived.duels.game.arena.IArena;
import club.revived.duels.service.cluster.Cluster;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Duel
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class Duel {

    private final DuelTeam blueTeam;
    private final DuelTeam redTeam;
    private final int rounds;
    private final KitType kitType;
    private final String id;
    private final IArena arena;

    private GameState gameState = GameState.PREPARING;

    private Game game;

    /**
     * Creates a Duel for the given blue and red team members using the specified rounds, kit, and arena.
     *
     * Also generates a unique duel identifier and initializes the backing Game instance.
     *
     * @param blueTeam list of UUIDs for blue team members
     * @param redTeam  list of UUIDs for red team members
     * @param rounds   total number of rounds for the duel
     * @param kitType  kit configuration to use for the duel
     * @param arena    arena where the duel will take place
     */
    public Duel(
            final List<UUID> blueTeam,
            final List<UUID> redTeam,
            final int rounds,
            final KitType kitType,
            final IArena arena
    ) {
        this.blueTeam = new DuelTeam(
                blueTeam,
                0,
                DuelTeamType.BLUE
        );

        this.redTeam = new DuelTeam(
                redTeam,
                0,
                DuelTeamType.RED
        );

        this.arena = arena;
        this.rounds = rounds;
        this.kitType = kitType;
        this.id = StringUtils.generateId("#game-");

        this.updateGame();
    }

    /**
     * Get the online Player objects for members of the blue team.
     *
     * @return a list of Player objects for blue team members who are currently online; an empty list if none are online
     */
    @NotNull
    public List<Player> getBluePlayers() {
        return this.blueTeam
                .getUuids()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Gets the online players who are members of the red team.
     *
     * @return a list of online players who are members of the red team; offline or unknown players are omitted
     */
    @NotNull
    public List<Player> getRedPlayers() {
        return this.redTeam
                .getUuids()
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
    @NotNull
    public List<Player> getPlayers() {
        return this.getUUIDs().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Determines whether the duel has completed because either team reached the configured number of rounds.
     *
     * @return {@code true} if either team's score equals the duel's rounds, {@code false} otherwise.
     */
    public boolean isOver() {
        return this.redTeam.getScore() == this.getRounds() ||
                this.blueTeam.getScore() == this.getRounds();
    }

    /**
     * Get all participant UUIDs for this duel.
     *
     * @return a new list containing all participant UUIDs; red team UUIDs appear first followed by blue team UUIDs.
     */
    @NotNull
    public List<UUID> getUUIDs() {
        // TODO: Replace, there's probably a better way to do this
        final var uuids = new ArrayList<>(this.redTeam.getUuids());
        uuids.addAll(this.blueTeam.getUuids());

        return uuids;
    }

    /**
     * Resolve which duel team the specified player belongs to.
     *
     * @param player the player to locate within the duel
     * @return the DuelTeam the player belongs to
     * @throws UnsupportedOperationException if the player is not a member of either team
     */
    @NotNull
    public DuelTeam getTeam(final Player player) {
        return this.getTeam(player.getUniqueId());
    }

    /**
     * Get the opposing DuelTeam for the provided team.
     *
     * @param team the team whose opponent to retrieve
     * @return `blueTeam` if the provided team's type is `BLUE`, otherwise `redTeam`
     */
    @NotNull
    public DuelTeam getOpposing(final DuelTeam team) {
        return team.getType() == DuelTeamType.BLUE ? this.redTeam : this.blueTeam;
    }

    /**
     * Determine which duel team contains the specified player UUID.
     *
     * @param uuid the player's UUID to resolve to a team
     * @return the red team if the UUID belongs to the red team, the blue team otherwise
     * @throws UnsupportedOperationException if the UUID is not a member of either team
     */
    @NotNull
    public DuelTeam getTeam(final UUID uuid) {
        if (!this.redTeam.getUuids().contains(uuid) &&
                !this.blueTeam.getUuids().contains(uuid)) {
            throw new UnsupportedOperationException();
        }

        return this.redTeam.getUuids().contains(uuid) ? this.redTeam : this.blueTeam;
    }

    /**
     * Removes the duel's current Game instance from the cluster-wide "games" global cache.
     */
    public void discard() {
        Cluster.getInstance().getGlobalCache().removeFromList(
                "games",
                this.game,
                1
        );
    }


    public void deleteGame() {
        Cluster.getInstance().getGlobalCache().removeFromList(
                "games",
                this.game,
                1
        );
    }

    /**
     * Refreshes the Duel's Game instance and synchronizes it with the global cache.
     *
     * Removes the current game entry from the global cache (if present), builds a new
     * Game using the duel's current teams, rounds, kit, game state, and id, assigns it
     * to the duel, and pushes the new Game into the global cache.
     */
    private void updateGame() {
        this.deleteGame();

        this.game = new Game(
                this.blueTeam.getUuids(),
                this.redTeam.getUuids(),
                this.rounds,
                this.kitType,
                this.gameState,
                this.id
        );

        Cluster.getInstance().getGlobalCache().push(
                "games",
                this.game
        );
    }

    /**
     * Get the blue team for this duel.
     *
     * @return the DuelTeam representing the blue side of the duel
     */
    public DuelTeam getBlueTeam() {
        return blueTeam;
    }

    /**
     * Get the red team for this duel.
     *
     * @return the DuelTeam representing the red side of the duel
     */
    public DuelTeam getRedTeam() {
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
     * Update the duel's current game state and refresh the registered Game.
     *
     * @param gameState the new GameState for this duel
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;

        this.updateGame();
    }

    /**
     * Accesses the Game instance that represents this duel.
     *
     * @return the current Game backing this Duel
     */
    public Game getGame() {
        return game;
    }

    /**
     * Get the arena associated with this duel.
     *
     * @return the {@link IArena} where the duel takes place
     */
    public IArena getArena() {
        return arena;
    }
}