package club.revived.lobby.game.kit;

import club.revived.lobby.database.DatabaseManager;
import club.revived.lobby.game.duel.KitType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PresetKitCache
 *
 * @author yyuh
 * @since 11.01.26
 */
public final class PresetKitCache {

    private final Map<KitType, KitTemplate> templates = new ConcurrentHashMap<>();
    private static PresetKitCache instance;

    public PresetKitCache() {
        instance = this;
        this.initialize();
    }

    private void initialize() {
        DatabaseManager.getInstance().getAll(KitTemplate.class)
                .thenAccept(kitTemplates -> {
                    for (final var kit : kitTemplates) {
                        this.templates.put(kit.kitType(), kit);
                    }
                });
    }

    public void update(
            final KitTemplate template
    ) {
        this.templates.put(template.kitType(), template);

        DatabaseManager.getInstance().save(KitTemplate.class, template);
    }

    @NotNull
    public KitTemplate get(final KitType kitType) {
        return this.templates.computeIfAbsent(kitType, type -> new KitTemplate(type, new HashMap<>()));
    }

    public static PresetKitCache getInstance() {
        if (instance == null) {
            return new PresetKitCache();
        }

        return instance;
    }

    public Map<KitType, KitTemplate> getTemplates() {
        return templates;
    }
}
