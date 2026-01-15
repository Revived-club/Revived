package club.revived.lobby.game.friends;

import java.util.List;
import java.util.UUID;

/**
 * FriendHolder
 *
 * @author yyuh
 * @since 15.01.26
 */
public record FriendHolder(UUID uuid, List<Friend> friends) {
}
