package club.revived.duels.game.duels;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.duels.Duels;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * DuelStartTask
 *
 * @author yyuh
 * @since 04.01.26
 */
public final class DuelStartTask extends BukkitRunnable {

    private final Duels instance = Duels.getInstance();

    private int cooldown;
    private final List<Player> players;
    private final Duel duel;
    private Consumer<Void> voidConsumer;

    public DuelStartTask(
            final int cooldown,
            final Duel duel
    ) {
        this.cooldown = cooldown;
        this.players = duel.getPlayers();
        this.duel = duel;
        this.runTaskTimer(instance, 0, 20);

        duel.setGameState(GameState.STARTING);
    }

    /**
     * Updates the duel start countdown UI for participating players and finalizes the duel start when it reaches zero.
     *
     * <p>On each invocation this updates player titles, action bar text and sounds to reflect the remaining
     * countdown. If the associated duel has ended or is marked over, the task cancels immediately. When the
     * countdown reaches zero the method displays the final "Fight" title, plays the start sound, executes an
     * optional callback on the main server thread, clears the duel's starting flag, and cancels the task.</p>
     */
    @Override
    public void run() {
        if (duel.getGameState() == GameState.ENDING ||
                duel.getGameState() == GameState.COMPLETED) {
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

            if (voidConsumer != null) {
                Bukkit.getScheduler().runTask(instance, () -> voidConsumer.accept(null));
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

    public void then(Consumer<Void> fallback) {
        voidConsumer = fallback;
    }
}
