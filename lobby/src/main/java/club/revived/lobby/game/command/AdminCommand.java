package club.revived.lobby.game.command;

import club.revived.lobby.game.WarpLocation;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.inventory.AdminPresetEditor;
import club.revived.lobby.game.kit.PresetKitCache;
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
                .then(new LiteralArgument("edit-kit")
                        .then(new MultiLiteralArgument("kit", KitType.toStringArray())
                                .executesPlayer((player, args) -> {
                                    final var kitString = (String) args.get("kit");
                                    final var kitType = KitType.valueOf(kitString);
                                    final var kit = PresetKitCache.getInstance().get(kitType);

                                    new AdminPresetEditor(
                                            player,
                                            kit
                                    );
                                })))
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
