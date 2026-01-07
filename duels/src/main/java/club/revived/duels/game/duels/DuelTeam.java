package club.revived.duels.game.duels;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DuelTeam {


    private final List<UUID> uuids;
    private int score;
    private final DuelTeamType type;

    /**
     * Create a DuelTeam with the given members, initial score, and team type.
     *
     * @param uuids member UUIDs that identify the players on this team
     * @param score initial score for the team
     * @param type  the DuelTeamType describing this team's role or side
     */
    public DuelTeam(
            List<UUID> uuids,
            int score,
            DuelTeamType type
    ) {
        this.uuids = uuids;
        this.score = score;
        this.type = type;
    }

    /**
     * Checks whether the given player is a member of this team.
     *
     * @param player the player to check for membership
     * @return `true` if the player's UUID is present in the team's UUID list, `false` otherwise
     */
    public boolean hasPlayer(final Player player) {
        return this.uuids.contains(player.getUniqueId());
    }

    /**
     * Get online Player instances corresponding to the team's UUIDs.
     *
     * Converts the stored UUIDs to Bukkit Player objects and excludes any players
     * that are not currently online.
     *
     * @return a list of online Players for the team's UUIDs; empty if none are online
     */
    @NotNull
    public List<Player> getPlayers() {
        return this.uuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Get the UUIDs identifying this team's members.
     *
     * @return the list of UUIDs identifying the team's members
     */
    public List<UUID> getUuids() {
        return uuids;
    }

    /**
     * Returns the team's current score.
     *
     * @return the team's current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Get the team's duel type.
     *
     * @return the team's {@link DuelTeamType}
     */
    public DuelTeamType getType() {
        return type;
    }

    /**
     * Adds the specified amount to the team's score.
     *
     * @param amount the amount to add to the current score; positive values increase the score, negative values decrease it
     */
    public void addScore(int amount) {
        this.score += amount;
    }

    /**
     * Set the team's score to the specified value.
     *
     * @param score the new score value to assign to the team
     */
    public void setScore(int score) {
        this.score = score;
    }
}