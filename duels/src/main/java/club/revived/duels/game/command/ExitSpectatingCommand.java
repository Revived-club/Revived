package club.revived.duels.game.command;

import club.revived.duels.game.duels.DuelManager;
import dev.jorel.commandapi.CommandTree;

public final class ExitSpectatingCommand {

  public ExitSpectatingCommand() {
    new CommandTree("exit")
        .executesPlayer((player, args) -> {
          if (!DuelManager.getInstance().isSpectating(player)) {
            player.sendRichMessage("<red>You are not spectating anyone");
            return;
          }

          DuelManager.getInstance().stopSpectating(player.getUniqueId());
        })
        .register("revived");
  }
}
