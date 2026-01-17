package club.revived.lobby.game.command;

import club.revived.lobby.game.command.argument.NetworkPlayerArgument;
import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;

public final class SpectateCommand {

  public SpectateCommand() {
    new CommandTree("spectate")
        .then(NetworkPlayerArgument.networkPlayer("target")
            .executesPlayer((player, args) -> {
              final var target = (NetworkPlayer) args.get("target");
              final var networkPlayer = PlayerManager.getInstance().getNetworkPlayers()
                  .get(player.getUniqueId());

              DuelManager.getInstance().startSpectating(networkPlayer, target);
            }))
        .register("revived");
  }
}
