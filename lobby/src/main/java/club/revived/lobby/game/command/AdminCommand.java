package club.revived.lobby.game.command;

import club.revived.lobby.game.WarpLocation;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;

/**
 * AdminCommand
 *
 * @author yyuh
 * @since 09.01.26
 */
public final class AdminCommand {

    public AdminCommand() {
        new CommandTree("admin")
                .withPermission("club.revived.admin")
                .then(new LiteralArgument("set-warp")
                        .withPermission("club.revived.admin.set-warp")
                        .then(new MultiLiteralArgument("warp", WarpLocation.toStringArray())
                                .executesPlayer((player, args) -> {
                                    final var warpString = (String) args.get("warp");
                                    final var warp = WarpLocation.fromId(warpString);

                                    warp.set(player.getLocation());
                                })
                        )).register("revived");
    }
}
