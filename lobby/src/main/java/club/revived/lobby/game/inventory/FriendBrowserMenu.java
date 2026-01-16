package club.revived.lobby.game.inventory;

import club.revived.commons.inventories.inv.ListMenu;
import club.revived.commons.inventories.inv.button.AbstractButton;
import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.inventories.util.HeadBuilder;
import club.revived.commons.inventories.util.Heads;
import club.revived.commons.inventories.util.ItemBuilder;
import club.revived.lobby.game.duel.Game;
import club.revived.lobby.game.friends.Friend;
import club.revived.lobby.game.friends.FriendHolder;
import club.revived.lobby.game.player.PlayerProfile;
import club.revived.lobby.game.player.PlayerProfileManager;
import club.revived.lobby.service.cluster.Cluster;
import club.revived.lobby.service.cluster.ServiceType;
import club.revived.lobby.service.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * FriendBrowserMenu
 *
 * @author yyuh - DL
 * @since 1/16/26
 */
public final class FriendBrowserMenu {

    public FriendBrowserMenu(
            final Player player,
            final FriendHolder friendHolder
    ) {
        this.friendItems(friendHolder.friends()).thenAccept(itemBuilders ->
                ListMenu.of("Your Friends")
                        .addItems(itemBuilders)
                        .open(player));

    }

    @NotNull
    private CompletableFuture<List<ItemBuilder>> friendItems(final List<Friend> friends) {
        return this.getFriendProfiles(friends)
                .thenApply(friendPlayerProfileMap -> friendPlayerProfileMap.entrySet().stream()
                        .map(entry -> {
                            final Friend friend = entry.getKey();
                            final PlayerProfile playerProfile = entry.getValue();

                            return ItemBuilder.item(Material.ACACIA_BOAT)
                                    .name(playerProfile.username())
                                    .lore(ColorUtils.parse("Friends Since: " + friend.friendsSince()));
                        })
                        .toList());
    }


    @NotNull
    private CompletableFuture<Map<Friend, PlayerProfile>> getFriendProfiles(final List<Friend> friends) {
        return PlayerProfileManager.getInstance()
                .getAll(friends.stream().map(Friend::uuid).toList())
                .thenApply(playerProfiles -> {
                    final Map<UUID, PlayerProfile> uuidToProfile = playerProfiles.stream()
                            .collect(Collectors.toMap(PlayerProfile::uuid, profile -> profile));

                    final Map<Friend, PlayerProfile> friendToProfile = new HashMap<>();

                    for (final Friend friend : friends) {
                        final PlayerProfile profile = uuidToProfile.get(friend.uuid());

                        if (profile != null) {
                            friendToProfile.put(friend, profile);
                        }
                    }

                    return friendToProfile;
                });
    }

}
