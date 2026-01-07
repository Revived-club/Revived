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

    public DuelTeam(
            List<UUID> uuids,
            int score,
            DuelTeamType type
    ) {
        this.uuids = uuids;
        this.score = score;
        this.type = type;
    }

    public boolean hasPlayer(final Player player) {
        return this.uuids.contains(player.getUniqueId());
    }

    @NotNull
    public List<Player> getPlayers() {
        return this.uuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<UUID> getUuids() {
        return uuids;
    }

    public int getScore() {
        return score;
    }

    public DuelTeamType getType() {
        return type;
    }

    public void addScore(int amount) {
        this.score += amount;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
