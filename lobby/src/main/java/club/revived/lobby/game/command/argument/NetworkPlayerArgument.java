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

    /**
     * Creates an argument that resolves an input username to the corresponding online NetworkPlayer and supplies completion suggestions.
     *
     * The argument validates the provided username against the current online players and suggests usernames of all online network players for tab-completion.
     *
     * @param nodeName the command node name for this argument
     * @return an Argument that parses an input username into a NetworkPlayer
     * @throws CustomArgument.CustomArgumentException if no online player matches the provided username (message: "Player not online: <name>")
     */
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