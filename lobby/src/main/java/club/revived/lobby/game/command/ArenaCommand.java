package club.revived.lobby.game.command;

import club.revived.commons.worldedit.WorldEditUtils;
import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.duel.schematic.ArenaType;
import club.revived.lobby.game.duel.schematic.DuelArenaDraft;
import club.revived.lobby.game.duel.schematic.DuelArenaSchematic;
import club.revived.lobby.game.duel.schematic.SchematicManager;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.messaging.impl.UpdateArenas;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ArenaCommand {

    private final Map<UUID, DuelArenaDraft> drafts = new HashMap<>();

    public ArenaCommand() {
        new CommandTree("arena")
                .withPermission("azareth.arenas")

                .then(new LiteralArgument("create")
                        .then(new StringArgument("id")
                                .then(new MultiLiteralArgument("type", ArenaType.toStringArray())
                                        .executesPlayer((player, args) -> {

                                            if (drafts.containsKey(player.getUniqueId())) {
                                                player.sendRichMessage("<red>You are already creating an arena.");
                                                return;
                                            }

                                            final Block[] selection = WorldEditUtils.getRegionCorners(player);
                                            if (selection == null) {
                                                player.sendRichMessage("<red>Invalid WorldEdit selection.");
                                                return;
                                            }

                                            final Location corner1 = selection[0].getLocation();
                                            final Location corner2 = selection[1].getLocation();

                                            final String id = (String) args.get("id");
                                            final var type = ArenaType.fromId((String) args.get("type"));

                                            final DuelArenaDraft draft = new DuelArenaDraft(
                                                    id,
                                                    corner1,
                                                    corner2,
                                                    type
                                            );

                                            drafts.put(player.getUniqueId(), draft);
                                            player.sendRichMessage("<green>Arena creation started.");
                                        }))))

                .then(new LiteralArgument("set-spawn")
                        .then(new IntegerArgument("index", 1, 2)
                                .executesPlayer((player, args) -> {
                                    final DuelArenaDraft draft = drafts.get(player.getUniqueId());

                                    if (draft == null) {
                                        player.sendRichMessage("<red>You are not creating an arena.");
                                        return;
                                    }

                                    final int index = (int) args.get("index");
                                    final Location location = player.getLocation();

                                    if (index == 1) {
                                        draft.setSpawn1(location);
                                    } else {
                                        draft.setSpawn2(location);
                                    }

                                    player.sendRichMessage("<green>Spawn " + index + " set.");
                                })))

                .then(new LiteralArgument("save")
                        .executesPlayer((player, args) -> {
                            final DuelArenaDraft draft = drafts.remove(player.getUniqueId());

                            if (draft == null) {
                                player.sendRichMessage("<red>You are not creating an arena.");
                                return;
                            }

                            if (!draft.isComplete()) {
                                player.sendRichMessage("<red>Both spawn points must be set.");
                                drafts.put(player.getUniqueId(), draft);
                                return;
                            }

                            SchematicManager.getInstance().saveArena(
                                    draft.getId(),
                                    draft.getCorner1(),
                                    draft.getCorner2(),
                                    draft.getSpawn1(),
                                    draft.getSpawn2(),
                                    draft.getArenaType()
                            );

                            player.sendRichMessage("<green>Arena saved successfully.");
                        }))
                .then(new LiteralArgument("update")
                        .executesPlayer((player, _) -> {
                            player.sendRichMessage("<green>Updating Arenas...");
                            Cluster.getInstance().getMessagingService()
                                    .sendGlobalMessage(new UpdateArenas());
                        }))
                .then(new LiteralArgument("list")
                        .executesPlayer((player, args) -> {
                            DatabaseManager.getInstance().getAll(DuelArenaSchematic.class)
                                    .thenAccept(duelArenaSchematics -> {
                                        for (final var arena : duelArenaSchematics) {
                                            player.sendRichMessage(arena.id());
                                        }
                                    });
                        }))

                .register("azareth");
    }
}
