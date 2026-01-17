package club.revived.lobby.game.command;

import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.inventory.KitTemplateEditorMenu;
import club.revived.lobby.game.kit.EditedKitHolder;
import club.revived.lobby.service.player.PlayerManager;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;

/**
 * KitEditorCommand
 *
 * @author yyuh
 * @since 17.01.26
 */
public final class KitEditorCommand {

    public KitEditorCommand() {
        new CommandTree("kiteditor")
                .then(new MultiLiteralArgument("kit", KitType.toStringArray())
                        .executesPlayer((player, args) -> {
                            final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);
                            final var kitString = (String) args.get("kit");
                            final var kitType = KitType.valueOf(kitString);

                            networkPlayer.getCachedOrLoad(EditedKitHolder.class).thenAccept(editedKitHolder -> {
                                if (editedKitHolder == null) {
                                    editedKitHolder = EditedKitHolder.newEmpty(player.getUniqueId());
                                }

                                final var kit = editedKitHolder.kits().get(kitType);
                                new KitTemplateEditorMenu(kit, player);
                            });
                        })).register("revived");
    }
}
