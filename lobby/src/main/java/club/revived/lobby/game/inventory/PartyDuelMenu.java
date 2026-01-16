package club.revived.lobby.game.inventory;

import club.revived.commons.generic.NumberUtils;
import club.revived.commons.inventories.impl.InventoryBuilder;
import club.revived.commons.inventories.inv.AbstractMenu;
import club.revived.commons.inventories.inv.AnvilMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.Lobby;
import club.revived.lobby.game.duel.KitType;
import club.revived.lobby.game.parties.Party;
import club.revived.lobby.game.parties.PartyManager;
import club.revived.lobby.service.exception.UnregisteredPlayerException;
import club.revived.lobby.service.player.PlayerManager;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Menu for sending starting party duels. Let's you change the kit & the amount of rounds
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class PartyDuelMenu {

    private static final int[] KIT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16, 20, 21, 23, 24
    };

    private final Party party;
    private final Player player;

    private int rounds = 1;
    private KitType kitType = KitType.SWORD;

    public PartyDuelMenu(final Player player, final Party party) {
        this.party = party;
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

    /**
     * Create the "Pick Rounds" inventory which lets the player choose a preset number of rounds or enter a custom amount.
     *
     * Selecting a preset or a valid custom amount updates this.rounds and reopens the settings menu for the initiating player.
     * Custom input is validated: it must be an integer and no greater than 20; invalid input results in an action-bar message.
     *
     * @return the InventoryBuilder for the rounds picker menu
     */
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

    /**
     * Create the "Edit Duel" inventory allowing review and modification of the pending duel before sending.
     *
     * The menu includes buttons to send or cancel the request, open the rounds picker, and display the selected kit and rounds.
     *
     * @return the InventoryBuilder for the "Edit Duel" menu
     * @throws UnregisteredPlayerException if the target player is not present in the network player map when attempting to send the request
     */
    @NotNull
    private InventoryBuilder settings() {
        return AbstractMenu.of(4, "Edit Duel")
                .button(new AbstractButton(10, ItemBuilder.item(Material.LIME_CANDLE).name("<green>Send Request"), event -> {
                    event.setCancelled(true);

                    final var networkPlayers = PlayerManager.getInstance()
                            .getNetworkPlayers();

                    if (!party.getMembers().contains(player.getUniqueId())) {
                        player.sendRichMessage("<red>You are not in the party! (haha imagine being kicked)");
                        throw new UnregisteredPlayerException("player does not exist");
                    }

                    if (!party.getOwner().equals(player.getUniqueId())) {
                        player.sendRichMessage("<red>You are not the owner of the party! (haha imagine being kicked)");
                        throw new UnregisteredPlayerException("player does not exist");
                    }

                    player.closeInventory();

                    PartyManager.getInstance().startGame(
                            party,
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