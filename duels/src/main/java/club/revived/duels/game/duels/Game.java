package club.revived.duels.game.duels;

import club.revived.duels.game.arena.IArena;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Game
 *
 * @author yyuh
 * @since 14.01.26
 */
public interface Game {

  void setGameState(final GameState gameState);

  GameState getGameState();

  GameData getData();

  List<Player> getPlayers();

  KitType getKitType();

  IArena getArena();

  List<UUID> getSpectators();

  List<Player> getSpectatingPlayers();

  void addSpectator(final UUID uuid);
}
