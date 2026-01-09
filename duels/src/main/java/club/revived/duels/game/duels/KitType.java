package club.revived.duels.game.duels;

import club.revived.duels.game.arena.ArenaType;
import org.bukkit.Material;

/**
 * KItType
 *
 * @author yyuh
 * @since 03.01.26
 */
public enum KitType {

    UHC(
            "UHC",
            false,
            Material.LAVA_BUCKET,
            ArenaType.INTERACTIVE
    ),

    SWORD(
            "Sword",
            false,
            Material.DIAMOND_SWORD,
            ArenaType.RESTRICTED
    ),

    MACE(
            "Mace",
            false,
            Material.MACE,
            ArenaType.RESTRICTED
    ),

    CART(
            "Cart",
            false,
            Material.TNT_MINECART,
            ArenaType.INTERACTIVE
    ),

    SMP(
            "SMP",
            false,
            Material.SHIELD,
            ArenaType.RESTRICTED
    ),

    NETHERITE_POTION(
            "Nethpot",
            false,
            Material.NETHERITE_CHESTPLATE,
            ArenaType.RESTRICTED
    ),

    DIAMOND_POTION(
            "Diapot",
            false,
            Material.DIAMOND_CHESTPLATE,
            ArenaType.RESTRICTED
    ),

    TNT(
            "TNT",
            false,
            Material.TNT,
            ArenaType.INTERACTIVE
    ),

    SPLEEF(
            "Spleef",
            false,
            Material.DIAMOND_SHOVEL,
            ArenaType.INTERACTIVE
    ),

    AXE(
            "Axe",
            false,
            Material.DIAMOND_AXE,
            ArenaType.RESTRICTED
    ),

    CRYSTAL(
            "Custom Kit",
            false,
            Material.END_CRYSTAL,
            ArenaType.INTERACTIVE

    ),

    DRAIN(
            "Drain",
            false,
            Material.END_CRYSTAL,
            ArenaType.INTERACTIVE
    ),

    EVAL("Eval",
            false,
            Material.END_CRYSTAL,
            ArenaType.INTERACTIVE
    );


    private final String bName;
    private final boolean isRanked;
    private final Material material;
    private final ArenaType arenaType;

    /**
     * Initializes a KitType with a display name, ranked status, visual material, and arena type.
     *
     * @param beautifiedName the human-friendly display name for the kit
     * @param ranked         `true` if the kit is considered ranked, `false` otherwise
     * @param material       the Bukkit Material used to represent the kit
     * @param arenaType      the arena context for the kit (e.g., INTERACTIVE or RESTRICTED)
     */
    KitType(
            final String beautifiedName,
            final boolean ranked,
            final Material material,
            final ArenaType arenaType
    ) {
        this.bName = beautifiedName;
        this.isRanked = ranked;
        this.material = material;
        this.arenaType = arenaType;
    }

    /**
     * Retrieve the Material associated with this kit type.
     *
     * @return the Bukkit Material that visually represents the kit in-game.
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Gets the human-friendly display name for this kit.
     *
     * @return the human-friendly display name of the kit
     */
    public String getBeautifiedName() {
        return this.bName;
    }

    /**
     * Indicates whether this kit type is ranked.
     *
     * @return {@code true} if the kit is ranked, {@code false} otherwise.
     */
    public boolean isRanked() {
        return this.isRanked;
    }

    /**
     * Get the arena type associated with this kit.
     *
     * @return the ArenaType that indicates the arena context for this kit
     */
    public ArenaType getArenaType() {
        return arenaType;
    }
}