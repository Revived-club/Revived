package club.revived.duels.game.duels;

import org.bukkit.scheduler.BukkitRunnable;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.location.BukkitCuboidRegion;
import club.revived.duels.game.arena.IArena;
import net.kyori.adventure.title.Title;

public final class ArenaScanTask extends BukkitRunnable {

  private final Game game;
  private final IArena arena;
  private final BukkitCuboidRegion cuboidRegion;

  public ArenaScanTask(final Game game) {
    this.game = game;
    this.arena = game.getArena();
    this.cuboidRegion = new BukkitCuboidRegion(
        arena.getCorner1(),
        arena.getCorner2());
  }

  @Override
  public void run() {
    for (final var player : this.game.getSpectatingPlayers()) {
      if (!this.cuboidRegion.contains(player.getLocation())) {
        player.showTitle(Title.title(
            ColorUtils.parse("<red>You can't leave the Arena!"),
            ColorUtils.empty()));
      }

      player.teleportAsync(arena.getCenter());
    }

    for (final var player : game.getPlayers()) {
      if (!this.cuboidRegion.contains(player.getLocation())) {
        player.setHealth(0.0);
        player.showTitle(Title.title(
            ColorUtils.parse("<red>You can't leave the Arena!"),
            ColorUtils.empty()));
      }
    }
  }
}
