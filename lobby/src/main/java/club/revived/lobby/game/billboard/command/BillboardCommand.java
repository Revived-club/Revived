package club.revived.lobby.game.billboard.command;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.billboard.BillboardManager;
import club.revived.lobby.game.billboard.QueueBillboardLocation;
import club.revived.lobby.game.duel.KitType;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.Location;

import java.util.Arrays;

public final class BillboardCommand {

    /**
     * Registers the "billboard" command tree used to manage queued billboards.
     *
     * <p>The command requires permission {@code club.revived.billboard} and provides
     * subcommands for operating on a billboard identified by a {@code KitType}:
     * "moveHere" (permission {@code club.revived.movehere}) — move or build the billboard at the command issuer's location;
     * "move" (permission {@code club.revived.move}) — move or build the billboard at a specified location;
     * "setYaw" (permission {@code club.revived.yaw}) — update a billboard's yaw;
     * "setPitch" (permission {@code club.revived.pitch}) — update a billboard's pitch.
     *
     * <p>Moving operations preserve the billboard's previous pitch/yaw where applicable and all changes are persisted
     * as {@code QueueBillboardLocation} entries.
     */
    public BillboardCommand() {
        new CommandTree("billboard")
                .withPermission("club.revived.billboard")
                .then(new MultiLiteralArgument("kitType", Arrays.stream(KitType.values()).map(KitType::name).toArray(String[]::new))
                        .then(new LiteralArgument("moveHere")
                                .withPermission("club.revived.movehere")
                                .executesPlayer((player, args) -> {
                                    final String kitTypeString = (String) args.get("kitType");
                                    final var kitType = KitType.valueOf(kitTypeString);
                                    final var location = player.getLocation();

                                    final var billboard = BillboardManager.getInstance()
                                            .getQueueBillboards()
                                            .remove(kitType);

                                    if (billboard == null) {
                                        BillboardManager.getInstance().build(kitType, location);
                                        return;
                                    }

                                    final var oldLoc = billboard.getLocation();

                                    location.setPitch(oldLoc.getPitch());
                                    location.setYaw(oldLoc.getYaw());

                                    billboard.move(location);
                                    BillboardManager.getInstance().getQueueBillboards().put(kitType, billboard);

                                    DatabaseManager.getInstance().save(QueueBillboardLocation.class, new QueueBillboardLocation(
                                            kitType,
                                            billboard.getLocation()
                                    ));
                                })
                        )
                        .then(new LiteralArgument("tp")
                                .withPermission("club.revived.tp")
                                .executesPlayer((player, args) -> {
                                    final String kitTypeString = (String) args.get("kitType");
                                    final var kitType = KitType.valueOf(kitTypeString);

                                    final var billboard = BillboardManager.getInstance()
                                            .getQueueBillboards()
                                            .remove(kitType);

                                    if (billboard == null) {
                                        player.sendRichMessage("<green>Billboard does not exist!");
                                        return;
                                    }

                                    player.teleportAsync(billboard.getLocation());
                                })

                        )
                        .then(new LiteralArgument("move")
                                .withPermission("club.revived.move")
                                .then(new LocationArgument("location")
                                        .executesPlayer((player, args) -> {
                                            final String kitTypeString = (String) args.get("kitType");
                                            final var kitType = KitType.valueOf(kitTypeString);
                                            final Location location = (Location) args.get("location");

                                            if (location == null) {
                                                return;
                                            }

                                            final var billboard = BillboardManager.getInstance()
                                                    .getQueueBillboards()
                                                    .remove(kitType);

                                            if (billboard == null) {
                                                BillboardManager.getInstance().build(kitType, location);
                                                return;
                                            }

                                            final var oldLoc = billboard.getLocation();

                                            location.setPitch(oldLoc.getPitch());
                                            location.setYaw(oldLoc.getYaw());

                                            billboard.move(location);
                                            BillboardManager.getInstance().getQueueBillboards().put(kitType, billboard);

                                            DatabaseManager.getInstance().save(QueueBillboardLocation.class, new QueueBillboardLocation(
                                                    kitType,
                                                    billboard.getLocation()
                                            ));
                                        })
                                )
                        )
                        .then(new LiteralArgument("setYaw")
                                .then(new FloatArgument("yaw")
                                        .withPermission("club.revived.yaw")
                                        .executesPlayer((player, args) -> {
                                            final String kitTypeString = (String) args.get("kitType");
                                            final var kitType = KitType.valueOf(kitTypeString);
                                            final float yaw = (float) args.get("yaw");

                                            final var billboard = BillboardManager.getInstance()
                                                    .getQueueBillboards()
                                                    .get(kitType);

                                            if (billboard == null) {
                                                throw new RuntimeException(player.getName() + " tried to move not existing billboard");
                                            }

                                            billboard.setYaw(yaw, player.getWorld());

                                            DatabaseManager.getInstance().save(QueueBillboardLocation.class, new QueueBillboardLocation(
                                                    kitType,
                                                    billboard.getLocation()
                                            ));
                                        })
                                )
                        )
                        .then(new LiteralArgument("setPitch")
                                .then(new FloatArgument("pitch")
                                        .withPermission("club.revived.pitch")
                                        .executesPlayer((player, args) -> {
                                            final String kitTypeString = (String) args.get("kitType");
                                            final var kitType = KitType.valueOf(kitTypeString);
                                            final float pitch = (float) args.get("pitch");

                                            final var billboard = BillboardManager.getInstance()
                                                    .getQueueBillboards()
                                                    .get(kitType);

                                            if (billboard == null) {
                                                throw new RuntimeException(player.getName() + " tried to move not existing billboard");
                                            }

                                            billboard.setPitch(pitch, player.getWorld());

                                            DatabaseManager.getInstance().save(QueueBillboardLocation.class, new QueueBillboardLocation(
                                                    kitType,
                                                    billboard.getLocation()
                                            ));
                                        })
                                )
                        )
                )
                .register("azareth");
    }
}