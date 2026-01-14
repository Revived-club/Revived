package club.revived.duels.game.duels.ffa;

import club.revived.commons.generic.ElapsedTimeFormatter;
import club.revived.commons.generic.StringUtils;
import club.revived.duels.game.arena.IArena;
import club.revived.duels.game.duels.Game;
import club.revived.duels.game.duels.GameData;
import club.revived.duels.game.duels.GameState;
import club.revived.duels.game.duels.KitType;
import club.revived.duels.service.cluster.Cluster;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Free-For-All Game
 *
 * @author yyuh
 * @since 14.01.26
 */
public final class FFA implements Game {

    private final List<UUID> participants;
    private final Map<UUID, Integer> scores;

    private final KitType kitType;
    private final IArena arena;
    private final String id;

    private final ElapsedTimeFormatter elapsedTimeFormatter;

    private GameState gameState = GameState.PREPARING;
    private GameData game;

    /**
     * Creates a new FFA game instance.
     *
     * @param participants list of participating player UUIDs
     * @param kitType      kit configuration
     * @param arena        arena used for this FFA
     */
    public FFA(
            final List<UUID> participants,
            final KitType kitType,
            final IArena arena
    ) {
        this.participants = List.copyOf(participants);
        this.kitType = kitType;
        this.arena = arena;

        this.scores = new ConcurrentHashMap<>();
        this.participants.forEach(uuid -> this.scores.put(uuid, 0));

        this.elapsedTimeFormatter = new ElapsedTimeFormatter();
        this.id = StringUtils.generateId("#ffa-");

        this.updateGame();
    }

    /**
     * Returns all online players participating in this FFA.
     *
     * @return list of online participants
     */
    @NotNull
    public List<Player> getPlayers() {
        return this.participants.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns all participant UUIDs.
     *
     * @return immutable list of UUIDs
     */
    @NotNull
    public List<UUID> getUUIDs() {
        return this.participants;
    }

    /**
     * Increments the score of a player.
     *
     * @param uuid the player UUID
     */
    public void addPoint(final UUID uuid) {
        this.scores.computeIfPresent(uuid, (k, v) -> v + 1);
    }

    /**
     * Returns the score for a given player.
     *
     * @param uuid the player UUID
     * @return the player's score
     */
    public int getScore(final UUID uuid) {
        return this.scores.getOrDefault(uuid, 0);
    }

    /**
     * Removes this FFA's game instance from the global cache.
     */
    public void discard() {
        Cluster.getInstance().getGlobalCache().removeFromList(
                "games",
                this.game,
                1
        );
    }

    private void updateGame() {
        this.discard();

        this.game = new GameData(
                this.participants,
                Collections.emptyList(),
                1,
                this.kitType,
                this.gameState,
                this.id
        );

        Cluster.getInstance().getGlobalCache().push(
                "games",
                this.game
        );
    }

    @Override
    public KitType getKitType() {
        return kitType;
    }

    public String getId() {
        return id;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public void setGameState(final GameState gameState) {
        this.gameState = gameState;
        this.updateGame();
    }

    @Override
    public GameData getData() {
        return this.game;
    }

    @Override
    public IArena getArena() {
        return arena;
    }

    public ElapsedTimeFormatter getElapsedTimeFormatter() {
        return elapsedTimeFormatter;
    }
}
