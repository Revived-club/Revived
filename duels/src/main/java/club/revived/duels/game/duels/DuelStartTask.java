package club.revived.duels.game.duels;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.duels.Duels;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;

/**
 * DuelStartTask
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelStartTask extends BukkitRunnable {

    private int cooldown;
    private final List<Player> players;
    private final Duel duel;

    /**
     * Initializes and schedules a countdown task that prepares and starts the given duel.
     *
     * The constructor stores the provided countdown value (in seconds), captures the duel's players,
     * schedules the task to run once per second, and sets the duel's game state to STARTING.
     *
     * @param cooldown the starting countdown value in seconds before the duel begins
     * @param duel the Duel instance this task will manage
     */
    public DuelStartTask(
            final int cooldown,
            final Duel duel
    ) {
        this.cooldown = cooldown;
        this.players = duel.getPlayers();
        this.duel = duel;
        this.runTaskTimer(Duels.getInstance(), 0, 20);

        duel.setGameState(GameState.STARTING);
    }

    /**
     * Advances the duel start countdown by one tick, updating players' titles and action-bar messages and
     * handling state transitions when the countdown finishes or the duel ends.
     *
     * <p>If the duel's game state is ENDING or DISCARDED the task cancels immediately. When the countdown
     * reaches zero a final "Fight" title is shown to all participating players, the duel's state is set
     * to RUNNING, and the task cancels. Otherwise the remaining seconds are shown to players and the
     * countdown value is decremented.</p>
     */
    @Override
    public void run() {
        if (duel.getGameState() == GameState.ENDING || duel.getGameState() == GameState.DISCARDED) {
            this.cancel();
            return;
        }

        if (cooldown < 1) {
            for (final Player player : players) {
                player.showTitle(Title.title(
                        ColorUtils.parse("<#3B82F6><bold>Fight"),
                        ColorUtils.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
                ));
            }

            duel.setGameState(GameState.RUNNING);
            this.cancel();
            return;
        }

        for (final Player player : players) {
            player.showTitle(Title.title(ColorUtils.parse("<#3B82F6><bold>" + cooldown), ColorUtils.parse("<#6aa2fc>Organize your Inventory"), Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)));
            player.sendActionBar(ColorUtils.parse("<white>Type <#d9071f>/ff <white>to <#d9071f>surrender<white> the duel"));
        }

        cooldown--;
    }
}