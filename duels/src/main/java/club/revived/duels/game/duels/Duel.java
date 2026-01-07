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
     * Constructs a Duel for the specified blue and red teams with the given number of rounds and kit type, and assigns a generated unique identifier.
     *
     * @param blueTeam list of UUIDs for blue team members
     * @param redTeam  list of UUIDs for red team members
     * @param rounds   the number of rounds for the duel
     * @param kitType  the kit configuration used in the duel
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
     * Get online Player objects for members of the red team.
     * <p>
     * Converts stored red-team UUIDs to Player instances and excludes members who are not currently online.
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

    public boolean isOver() {
        return this.redTeam.getScore() == this.getRounds() ||
                this.blueTeam.getScore() == this.getRounds();
    }

    @NotNull
    public List<UUID> getUUIDs() {
        // TODO: Replace, there's probably a better way to do this
        final var uuids = new ArrayList<>(this.redTeam.getUuids());
        uuids.addAll(this.blueTeam.getUuids());

        return uuids;
    }

    @NotNull
    public DuelTeam getTeam(final Player player) {
        return this.getTeam(player.getUniqueId());
    }

    @NotNull
    public DuelTeam getOpposing(final DuelTeam team) {
        return team.getType() == DuelTeamType.BLUE ? this.blueTeam : this.redTeam;
    }

    @NotNull
    public DuelTeam getTeam(final UUID uuid) {
        if (!this.redTeam.getUuids().contains(uuid) &&
                !this.blueTeam.getUuids().contains(uuid)) {
            throw new UnsupportedOperationException();
        }

        return this.redTeam.getUuids().contains(uuid) ? this.redTeam : this.blueTeam;
    }

    public void discard() {
        Cluster.getInstance().getGlobalCache().removeFromList(
                "games",
                this.game,
                1
        );
    }


    private void updateGame() {
        Cluster.getInstance().getGlobalCache().removeFromList(
                "games",
                this.game,
                1
        );

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
     * Returns the UUIDs of players assigned to the blue team.
     *
     * @return a list of UUIDs representing the blue team members
     */
    public DuelTeam getBlueTeam() {
        return blueTeam;
    }

    /**
     * Retrieve the red team's member UUIDs.
     *
     * @return the list of UUIDs representing players on the red team
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
     * Set the duel's current game state.
     *
     * @param gameState the new state for this duel
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;

        this.updateGame();
    }

    public Game getGame() {
        return game;
    }

    public IArena getArena() {
        return arena;
    }
}