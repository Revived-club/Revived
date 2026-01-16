package club.revived.lobby.game.player;

import java.util.UUID;

/**
 * PlayerProfile
 *
 * @author yyuh - DL
 * @since 1/16/26
 */
public record PlayerProfile(
        UUID uuid,
        String username,
        String skin,
        long lastLogin
) {}
