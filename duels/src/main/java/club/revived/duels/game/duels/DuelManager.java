package club.revived.duels.game.duels;

import club.revived.commons.inventories.util.ColorUtils;
import club.revived.commons.location.BukkitCuboidRegion;
import club.revived.commons.player.PlayerJoinTracker;
import club.revived.duels.Duels;
import club.revived.duels.game.arena.IArena;
import club.revived.duels.game.arena.pooling.ArenaPoolManager;
import club.revived.duels.game.duels.ffa.FFA;
import club.revived.duels.game.kit.EditedDuelKit;
import club.revived.duels.service.cluster.Cluster;
import club.revived.duels.service.cluster.ServiceType;
import club.revived.duels.service.messaging.impl.*;
import club.revived.duels.service.player.NetworkPlayer;
import club.revived.duels.service.player.PlayerManager;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DuelManager
 *
 * @author yyuh
 * @since 03.01.26
 */
public final class DuelManager {

  private final Map<UUID, Game> runningGames = new ConcurrentHashMap<>();
  private final Map<UUID, Game> spectating = new ConcurrentHashMap<>();

  private final Cluster cluster = Cluster.getInstance();

  private static DuelManager instance;

  /**
   * Initializes the DuelManager singleton and registers message handlers for duel
   * lifecycle messages.
   * <p>
   * Sets the static instance reference and registers handlers for DuelStart and
   * MigrateGame with the cluster messaging service.
   */
  public DuelManager() {
    instance = this;

    this.cluster.getMessagingService().registerMessageHandler(DuelStart.class, this::startDuel);
    this.cluster.getMessagingService().registerMessageHandler(MigrateGame.class, this::migrateGame);
    this.cluster.getMessagingService().registerMessageHandler(FFAStart.class, this::startFFA);
    this.cluster.getMessagingService().registerMessageHandler(StartSpectating.class, this::startSpectating);
    this.cluster.getMessagingService().registerHandler(IsDuelingRequest.class, request -> {
      final var uuid = request.uuid();
      final var isDueling = this.runningGames.containsKey(uuid);

      return new IsDuelingResponse(
          uuid,
          isDueling ? this.runningGames.get(uuid).getData().id() : "",
          isDueling);
    });
  }

  private void startSpectating(final StartSpectating startSpectating) {
    final var networkPlayer = PlayerManager.getInstance().getNetworkPlayers()
        .get(startSpectating.uuid());

    final var game = this.byId(startSpectating.duelId());
    final var arena = game.getArena();

    networkPlayer.connectHere();

    PlayerJoinTracker.of(Duels.getInstance(), List.of(startSpectating.uuid()), online -> {
      final var player = online.getFirst();
      player.setGameMode(GameMode.SPECTATOR);
      player.teleportAsync(arena.getCenter());

      for (final var gaming : game.getPlayers()) {
        gaming.sendRichMessage(String.format("<gray>» %s started spectating your Game!", player.getName()));
      }

      game.addSpectator(player.getUniqueId());

      this.spectating.put(player.getUniqueId(), game);
      player.sendRichMessage("<green>Successfully started spectating duel");
    });
  }

  public void stopSpectating(final UUID uuid) {
    if (!this.runningGames.containsKey(uuid)) {
      throw new UnsupportedOperationException("Trying to end non existing spectating session");
    }

    final var lobby = Cluster.getInstance().getLeastLoadedService(ServiceType.LOBBY);
    final var networkPlayer = PlayerManager.getInstance().getNetworkPlayers().get(uuid);

    networkPlayer.sendMessage("<red>Successfully stopped spectating!");
    networkPlayer.connect(lobby.getId());
  }

  /**
   * Reconstructs and launches a duel from a migrated game state.
   *
   * <p>
   * Creates a Duel from the migration payload, restores team scores, moves each
   * participant
   * to the current service, applies saved inventories (EditedDuelKit), teleports
   * players to
   * arena spawns, registers participants in the active-duel registry, and
   * initiates the duel
   * start countdown.
   *
   * @param game the migration payload containing team compositions, scores, kit
   *             type, rounds,
   *             and source server identifier
   */
  private void migrateGame(final MigrateGame game) {
    final var blueTeam = game.blueTeam();
    final var redTeam = game.redTeam();

    final var maxRounds = game.maxRounds();
    final var kitType = game.kitType();

    final int blueScore = game.blueScore();
    final int redScore = game.redScore();

    ArenaPoolManager.getInstance().getArena(kitType).thenAccept(arena -> {
      final var duel = new Duel(
          blueTeam,
          redTeam,
          maxRounds,
          kitType,
          arena);

      duel.getBlueTeam().setScore(blueScore);
      duel.getRedTeam().setScore(redScore);

      final var networkPlayers = duel.getUUIDs().stream()
          .map(uuid -> PlayerManager.getInstance().fromBukkitPlayer(uuid))
          .toList();

      networkPlayers.forEach(networkPlayer -> {
        networkPlayer.sendMessage("<red>There has been an issue with " + game.gameServerId());
        networkPlayer.connectHere();
        this.runningGames.put(networkPlayer.getUuid(), duel);
      });

      PlayerJoinTracker.of(Duels.getInstance(), duel.getUUIDs(), players -> {
        for (final var player : players) {
          player.sendRichMessage("""

              <#3B82F6><bold>Duel Info<reset>
              <white>First To: <#3B82F6><to>
              <white>Duel Kit: <#3B82F6><kit>
              <white>Players: <#3B82F6><players>

              """
              .replace("<kit>", kitType.getBeautifiedName())
              .replace("<to>", String.valueOf(maxRounds))
              .replace("<players>", String.join(", ", players
                  .stream()
                  .map(Player::getName)
                  .toArray(String[]::new))));

          this.healPlayer(player);

          final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

          networkPlayer.getCachedOrLoad(EditedDuelKit.class).thenAccept(editedDuelKit -> player.getInventory()
              .setContents(editedDuelKit.content().values().toArray(new ItemStack[0])));

          this.runningGames.put(player.getUniqueId(), duel);
        }

        for (final Player redPlayer : duel.getRedPlayers()) {
          redPlayer.teleportAsync(arena.getSpawn1().add(0, 1, 0));
        }

        for (final Player bluePlayer : duel.getBluePlayers()) {
          bluePlayer.teleportAsync(arena.getSpawn2().add(0, 1, 0));
        }

        new ArenaScanTask(duel).runTaskTimer(Duels.getInstance(), 20L, 20L);
        new GameStartTask(3, duel);
      });
    });
  }

  public void startFFA(
      final FFAStart ffaStart) {
    final KitType kitType = ffaStart.kitType();

    ArenaPoolManager.getInstance().getArena(kitType).thenAccept(arena -> {
      final var ffa = new FFA(
          ffaStart.players(),
          ffaStart.kitType(),
          arena);

      final var networkPlayers = ffa.getUUIDs().stream()
          .map(uuid -> PlayerManager.getInstance().fromBukkitPlayer(uuid))
          .toList();

      networkPlayers.forEach(NetworkPlayer::connectHere);

      PlayerJoinTracker.of(Duels.getInstance(), ffa.getUUIDs(), players -> {
        for (final var player : players) {
          player.sendRichMessage("""

              <#3B82F6><bold>FFA Info<reset>
              <white>Duel Kit: <#3B82F6><kit>
              <white>Players: <#3B82F6><players>

              """
              .replace("<kit>", kitType.getBeautifiedName())
              .replace("<players>", String.join(", ", players
                  .stream()
                  .map(Player::getName)
                  .toArray(String[]::new))));

          this.healPlayer(player);

          final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

          networkPlayer.getCachedOrLoad(EditedDuelKit.class).thenAccept(editedDuelKit -> player.getInventory()
              .setContents(editedDuelKit.content().values().toArray(new ItemStack[0])));

          this.runningGames.put(networkPlayer.getUuid(), ffa);
        }

        for (final Player player : ffa.getPlayers()) {
          player.teleportAsync(arena.getCenter());
        }

        new ArenaScanTask(ffa).runTaskTimer(Duels.getInstance(), 20L, 20L);
        new GameStartTask(3, ffa);
      });
    });
  }

  /**
   * Initiates a duel using the provided DuelStart message.
   * <p>
   * </p>
   * Reserves an arena, creates and registers the Duel, prepares and teleports
   * participants,
   * loads their edited kits, heals players, and begins the duel countdown.
   *
   * @param duelStart message containing the blue and red team UUID lists, the
   *                  number of rounds,
   *                  and the kit type to use for the duel
   */
  private void startDuel(final DuelStart duelStart) {
    final List<UUID> blueTeam = duelStart.blueTeam();
    final List<UUID> redTeam = duelStart.redTeam();

    final int rounds = duelStart.rounds();
    final KitType kitType = duelStart.kitType();

    ArenaPoolManager.getInstance().getArena(kitType).thenAccept(arena -> {
      final var duel = new Duel(
          blueTeam,
          redTeam,
          rounds,
          kitType,
          arena);

      // TODO: Replace? Cant think of a better solution rn :3 :§ :§ § :3
      final var uuids = new HashSet<>(redTeam);
      uuids.addAll(blueTeam);

      final var networkPlayers = uuids.stream()
          .map(uuid -> PlayerManager.getInstance().fromBukkitPlayer(uuid))
          .toList();

      System.out.println(networkPlayers);

      networkPlayers.forEach(networkPlayer -> {
        networkPlayer.connectHere();
        this.runningGames.put(networkPlayer.getUuid(), duel);
      });

      PlayerJoinTracker.of(Duels.getInstance(), uuids, players -> {
        for (final var player : players) {
          player.sendRichMessage("""

              <#3B82F6><bold>Duel Info<reset>
              <white>First To: <#3B82F6><to>
              <white>Duel Kit: <#3B82F6><kit>
              <white>Players: <#3B82F6><players>
              """
              .replace("<kit>", kitType.getBeautifiedName())
              .replace("<to>", String.valueOf(rounds))
              .replace("<players>", String.join(", ", players
                  .stream()
                  .map(Player::getName)
                  .toArray(String[]::new))));

          this.healPlayer(player);

          final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

          networkPlayer.getCachedOrLoad(EditedDuelKit.class).thenAccept(editedDuelKit -> player.getInventory()
              .setContents(editedDuelKit.content().values().toArray(new ItemStack[0])));

          this.runningGames.put(player.getUniqueId(), duel);
        }

        for (final Player redPlayer : duel.getRedPlayers()) {
          redPlayer.teleportAsync(arena.getSpawn1().add(0, 1, 0));
        }

        for (final Player bluePlayer : duel.getBluePlayers()) {
          bluePlayer.teleportAsync(arena.getSpawn2().add(0, 1, 0));
        }

        new ArenaScanTask(duel).runTaskTimer(Duels.getInstance(), 20L, 20L);
        new GameStartTask(3, duel);
      });
    });
  }

  /**
   * Finalizes a duel and notifies the lobby of its outcome.
   * <p>
   * Sets the duel state to ENDING, removes all participants from the active-duel
   * registry and heals them,
   * then sends a DuelEnd message to the least-loaded lobby service containing
   * winner and loser UUIDs,
   * rounds, final scores, and the duel's kit type.
   *
   * @param duel   the duel to finalize
   * @param winner the team that won the duel
   * @param loser  the team that lost the duel
   */
  public void endDuel(
      final Duel duel,
      final DuelTeam winner,
      final DuelTeam loser) {
    duel.setGameState(GameState.ENDING);

    final String winners = winner.getPlayers().stream()
        .map(Player::getName)
        .collect(Collectors.joining(", "));

    final String losers = loser.getPlayers().stream()
        .map(Player::getName)
        .collect(Collectors.joining(", "));

    for (final var spectator : duel.getSpectatingPlayers()) {
      spectator.showTitle(Title.title(
          ColorUtils.parse("<gold><bold>Duel Ended!"),
          ColorUtils.empty()));

      spectator.sendRichMessage(String.format("<green>%s won the duel", winners));
    }

    for (final var player : duel.getPlayers()) {
      this.runningGames.remove(player.getUniqueId());
      this.healPlayer(player);

      player.sendRichMessage("""

          <#3B82F6><bold>Duel Summary</bold><reset>

          <white>Kit:</white> <#3B82F6>%s
          <white>Score:</white> <#3B82F6>%d</#3B82F6> - <#EF4444>%d</#EF4444>
          <white>Duration:</white> <#3B82F6>%s
          <white>Winners: <#3B82F6>%s
          <white>Losers: <#3B82F6>%s
          """.formatted(
          duel.getKitType().getBeautifiedName(),
          winner.getScore(),
          loser.getScore(),
          duel.getElapsedTimeFormatter().getElapsedTime(),
          winners,
          losers));
    }

    Bukkit.getScheduler().runTaskLater(Duels.getInstance(), () -> {

      duel.deleteGame();

      for (final var player : duel.getSpectatingPlayers()) {
        this.spectating.remove(player.getUniqueId());
        final var service = Cluster.getInstance().getLeastLoadedService(ServiceType.LOBBY);

        final var networkPlayer = PlayerManager.getInstance()
            .fromBukkitPlayer(player);

        networkPlayer.connect(service);
      }

      this.cluster.getLeastLoadedService(ServiceType.LOBBY)
          .sendMessage(new DuelEnd(
              winner.getUuids(),
              loser.getUuids(),
              duel.getRounds(),
              winner.getScore(),
              loser.getScore(),
              duel.getKitType(),
              0L // TODO: Impl
      ));
    }, 60L);
  }

  public void endFFA(
      final FFA ffa,
      final Player winner) {
    ffa.setGameState(GameState.ENDING);

    for (final var spectator : ffa.getSpectatingPlayers()) {
      spectator.showTitle(Title.title(
          ColorUtils.parse("<gold><bold>Duel Ended!"),
          ColorUtils.empty()));

      spectator.sendRichMessage(String.format("<green>%s won the FFA", winner.getPlayer().getName()));
    }

    for (final var player : ffa.getPlayers()) {
      this.runningGames.remove(player.getUniqueId());
      this.healPlayer(player);

      player.sendRichMessage("""

          <#3B82F6><bold>FFA Summary</bold><reset>

          <white>Kit:</white> <#3B82F6>%s
          <white>Duration:</white> <#3B82F6>%s
          <white>Winner: <#3B82F6>%s
          """.formatted(
          ffa.getKitType().getBeautifiedName(),
          ffa.getElapsedTimeFormatter().getElapsedTime(),
          winner.getName()));
    }

    Bukkit.getScheduler().runTaskLater(Duels.getInstance(), () -> {
      ffa.discard();

      for (final var player : ffa.getSpectatingPlayers()) {
        this.spectating.remove(player.getUniqueId());
        final var service = Cluster.getInstance().getLeastLoadedService(ServiceType.LOBBY);

        final var networkPlayer = PlayerManager.getInstance()
            .fromBukkitPlayer(player);

        networkPlayer.connect(service);
      }

      this.cluster.getLeastLoadedService(ServiceType.LOBBY)
          .sendMessage(new FFAEnd(
              winner.getUniqueId(),
              ffa.getUUIDs(),
              ffa.getKitType(),
              0L));
    }, 60L);
  }

  /**
   * Initiates the next round for the provided duel. Currently a placeholder and
   * performs no action.
   *
   * @param duel the duel to start a new round for
   */
  public void startNewRound(final Duel duel) {
    final IArena selected = duel.getArena();

    final var region = new BukkitCuboidRegion(
        selected.getCorner1(),
        selected.getCorner2());

    for (final var entity : region.getEntities()) {
      if (entity.getType() == EntityType.PLAYER) {
        continue;
      }

      entity.remove();
    }

    for (final Player redPlayer : duel.getRedPlayers()) {
      redPlayer.teleportAsync(selected.getSpawn1());
    }

    for (final Player bluePlayer : duel.getBluePlayers()) {
      bluePlayer.teleportAsync(selected.getSpawn2());
    }

    for (final var player : duel.getPlayers()) {
      player.setGameMode(GameMode.SURVIVAL);
      this.healPlayer(player);

      final var networkPlayer = PlayerManager.getInstance().fromBukkitPlayer(player);

      networkPlayer.getCachedOrLoad(EditedDuelKit.class).thenAccept(editedDuelKit -> player.getInventory()
          .setContents(editedDuelKit.content().values().toArray(new ItemStack[0])));
    }

    new GameStartTask(3, duel);
  }

  /**
   * Restore a player's health, hunger, exhaustion, potion effects, and extinguish
   * fire.
   * <p>
   * The restoration is performed on the server's main thread.
   *
   * @param player the Bukkit player to restore
   */
  private void healPlayer(final Player player) {
    Bukkit.getScheduler().runTask(Duels.getInstance(), () -> {
      player.heal(20.0);
      player.setExhaustion(0);
      player.setFoodLevel(20);
      player.clearActivePotionEffects();
      player.setFireTicks(0);
    });
  }

  public boolean isSpectating(final Player player) {
    return this.isSpectating(player.getUniqueId());
  }

  public boolean isSpectating(final UUID uuid) {
    return this.spectating.containsKey(uuid);
  }

  /**
   * Checks whether the given player is currently participating in an active duel.
   *
   * @param player the player to check
   * @return `true` if the player is currently in an active duel, `false`
   *         otherwise
   */
  public boolean isPlaying(final Player player) {
    return this.isPlaying(player.getUniqueId());
  }

  /**
   * Determines whether the player identified by the given UUID is currently in an
   * active duel.
   *
   * @param uuid the player's UUID to check
   * @return `true` if the player with the given UUID is in an active duel,
   *         `false` otherwise
   */
  public boolean isPlaying(final UUID uuid) {
    return this.runningGames.containsKey(uuid);
  }

  /**
   * Retrieve the duel the given player is currently participating in, if any.
   *
   * @param player the player whose duel to retrieve
   * @return the player's active {@link Duel}, or `null` if the player is not in a
   *         duel
   */
  @Nullable
  public Game getDuel(final Player player) {
    return this.getGame(player.getUniqueId());
  }

  /**
   * Retrieve the active duel for a participant by UUID.
   *
   * @param uuid the participant's UUID
   * @return the Duel the participant is currently in, or {@code null} if none
   */
  @Nullable
  public Game getGame(final UUID uuid) {
    return this.runningGames.get(uuid);
  }

  public Map<UUID, Game> getRunningGames() {
    return runningGames;
  }

  @Nullable
  public Game byId(final String id) {
    return this.runningGames.values().stream()
        .filter(game -> game.getData().id().equals(id))
        .toList()
        .getFirst();
  }

  /**
   * Provides the singleton DuelManager instance, creating and initializing it if
   * none exists.
   *
   * @return the singleton DuelManager instance
   */
  public static DuelManager getInstance() {
    if (instance == null) {
      return new DuelManager();
    }

    return instance;
  }
}
