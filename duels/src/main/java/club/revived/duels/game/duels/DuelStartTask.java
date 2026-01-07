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
     * Creates and schedules a duel start countdown task for the given duel.
     * <p></p>
     * Initializes the task with the provided countdown, captures the duel's players, schedules the task to run once per second, and transitions the duel's game state to STARTING.
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
     * Advances the duel start countdown: updates player titles and action-bar messages each second,
     * transitions the duel to RUNNING when the countdown completes, and cancels the task if the duel ends.
     *
     * <p>Each tick this updates participating players' titles and action bar with the remaining time.
     * If the duel's state is ENDING or COMPLETED the task cancels immediately. When the countdown reaches
     * zero a final "Fight" title is shown to all players, an optional registered callback is executed on
     * the main server thread, the duel's state is set to RUNNING, and the task cancels.</p>
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