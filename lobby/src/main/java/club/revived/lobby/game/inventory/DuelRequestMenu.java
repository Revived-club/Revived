package club.revived.lobby.game.inventory;

import club.revived.commons.NumberUtils;
import club.revived.commons.inventories.impl.InventoryBuilder;
import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.AnvilMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.Lobby;
import club.revived.lobby.game.duel.DuelManager;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.player.PlayerManager;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DuelRequestMenu
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DuelRequestMenu {

    private static final int[] KIT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16, 20, 21, 23, 24
    };

    private final UUID target;
    private final Player player;

    private int rounds = 1;
    private KitType kitType = KitType.SWORD;

    public DuelRequestMenu(final Player player, final UUID target) {
        this.target = target;
        this.player = player;

        final var menu = AbstractMenu.of(4, "Select Kit");

        final var kits = KitType.values();

        for (int i = 0; i < kits.length; i++) {
            if (i >= KIT_SLOTS.length) {
                break;
            }

            final var kit = kits[i];
            final int slot = KIT_SLOTS[i];

            menu.button(new AbstractButton(slot, ItemBuilder.item(kit.getMaterial())
                    .name("<#3B82F6>" + kit.getBeautifiedName()), event -> {
                event.setCancelled(true);
                this.kitType = kit;
                this.settings().open(player);
            }));
        }

        menu.open(player);
    }

    @NotNull
    private InventoryBuilder roundPicker() {
        final var menu = AbstractMenu.of(4, "Pick Rounds");

        for (int i = 10; i < 17; i++) {
            final int finalI = i;

            menu.button(new AbstractButton(i, ItemBuilder.item(Material.PAPER)
                    .amount(i - 9)
                    .name("<#3B82F6>" + (i - 9) + " Rounds"), event -> {
                event.setCancelled(true);
                this.rounds = (finalI - 9);
                this.settings().open(player);
            }));
        }

        menu.button(new AbstractButton(22, ItemBuilder.item(Material.ANVIL).name("<#3B82F6>Pick Custom Amount"), event -> {
            event.setCancelled(true);
            AnvilMenu.of(Lobby.getInstance())
                    .setTitle("Picking Rounds")
                    .setText("Enter Amount")
                    .onClick((slot, state) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) {
                            return Collections.emptyList();
                        }

                        final String newName = state.getText();

                        if (!NumberUtils.isInteger(newName)) {
                            return List.of(AnvilGUI.ResponseAction.run(() -> {
                                player.sendActionBar(ColorUtils.parse("<red>You have to enter a Number!"));
                            }));
                        }

                        final int rounds = Integer.parseInt(newName);

                        if (rounds > 20) {
                            return List.of(AnvilGUI.ResponseAction.run(() -> {
                                player.sendActionBar(ColorUtils.parse("<red>The amount of Rounds can't exceed 20!"));
                            }));
                        }

                        return List.of(AnvilGUI.ResponseAction.close(),
                                AnvilGUI.ResponseAction.run(() -> {
                                    this.rounds = rounds;
                                    this.settings().open(player);
                                }));
                    })
                    .preventClose()
                    .open(player);
        }));

        return menu;
    }

    @NotNull
    private InventoryBuilder settings() {
        return AbstractMenu.of(4, "Edit Duel")
                .button(new AbstractButton(10, ItemBuilder.item(Material.LIME_CANDLE).name("<green>Send Request"), event -> {
                    event.setCancelled(true);

                    final var networkPlayers = PlayerManager.getInstance()
                            .getNetworkPlayers();

                    if (!networkPlayers.containsKey(this.target)) {
                        player.sendRichMessage(String.format("%s: Player is offline or not registered", UnregisteredPlayerException.class.getSimpleName().toUpperCase()));
                        throw new UnregisteredPlayerException("player does not exist");
                    }

                    final var receiver = networkPlayers.get(this.target);
                    final var sender = PlayerManager.getInstance().fromBukkitPlayer(this.player);

                    DuelManager.getInstance().requestDuel(
                            sender,
                            receiver,
                            this.rounds,
                            this.kitType
                    );
                }))
                .button(new AbstractButton(16, ItemBuilder.item(Material.RED_CANDLE).name("<red>Cancel Request"), event -> {
                    event.setCancelled(true);
                    player.closeInventory();
                }))
                .button(new AbstractButton(13, ItemBuilder.item(Material.CLOCK)
                        .name("<#3B82F6>Rounds: " + rounds)
                        .lore(ColorUtils.parse("<white>Click this to set the amount of rounds to win to win the duel.")), event -> {
                    event.setCancelled(true);
                    this.roundPicker().open(player);
                }))
                .button(new AbstractButton(21, ItemBuilder.item(Material.CREEPER_BANNER_PATTERN).addFlag(ItemFlag.HIDE_ATTRIBUTES).name("<#3B82F6>Request Information")
                        .lore(
                                ColorUtils.parse("<white>Kit: <#3B82F6>" + this.kitType.getBeautifiedName()),
                                ColorUtils.parse("<white>Rounds: <#3B82F6>" + this.rounds)
                        ), event -> event.setCancelled(true)
                ))
                .fillEmpty(ItemBuilder.placeholder());
    }
}