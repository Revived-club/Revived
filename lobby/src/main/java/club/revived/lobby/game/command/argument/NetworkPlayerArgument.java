package club.revived.lobby.game.command.argument;

import club.revived.lobby.service.player.NetworkPlayer;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

/**
 * NetworkPlayerArgument
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class NetworkPlayerArgument {

    public static Argument<NetworkPlayer> networkPlayer(final String nodeName) {
        return new CustomArgument<>(
                new StringArgument(nodeName),
                info -> {
                    final var name = info.input();

                    final var networkPlayer = PlayerManager.getInstance().withName(name);

                    if (networkPlayer == null) {
                        throw CustomArgument.CustomArgumentException.fromString("Player not online: " + name);
                    }

                    return networkPlayer;
                }
        ).replaceSuggestions(ArgumentSuggestions.strings(
                info -> PlayerManager.getInstance().getNetworkPlayers()
                        .values()
                        .stream()
                        .map(NetworkPlayer::getUsername)
                        .toArray(String[]::new)));
    }
}
