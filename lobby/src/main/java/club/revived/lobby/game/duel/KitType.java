package club.revived.lobby.game.duel;

import org.bukkit.Material;

/**
 * Different Kits mostly mctiers kits.
 *
 * @author yyuh
 * @since 03.01.26
 */
public enum KitType {

    UHC(
            "UHC",
            false,
            Material.LAVA_BUCKET
    ),

    SWORD(
            "Sword",
            false,
            Material.DIAMOND_SWORD
    ),

    MACE(
            "Mace",
            false,
            Material.MACE
    ),

    CART(
            "Cart",
            false,
            Material.TNT_MINECART
    ),

    SMP(
            "SMP",
            false,
            Material.SHIELD
    ),

    NETHERITE_POTION(
            "Nethpot",
            false,
            Material.NETHERITE_CHESTPLATE
    ),

    DIAMOND_POTION(
            "Diapot",
            false,
            Material.DIAMOND_CHESTPLATE
    ),

    TNT(
            "TNT",
            false,
            Material.TNT),

    SPLEEF(
            "Spleef",
            false,
            Material.DIAMOND_SHOVEL
    ),

    AXE(
            "Axe",
            false,
            Material.DIAMOND_AXE
    ),

    CRYSTAL(
            "Custom Kit",
            false,
            Material.END_CRYSTAL

    ),

    DRAIN(
            "Drain",
            false,
            Material.END_CRYSTAL
    ),

    EVAL("Evaluation",
            false,
            Material.END_CRYSTAL
    );


    private final String bName;
    private final boolean isRanked;
    private final Material material;

    /**
     * Creates a KitType with the specified display name, ranked flag, and associated material.
     *
     * @param beautifiedName the human-friendly name shown for the kit
     * @param ranked         whether the kit is considered ranked
     * @param material       the Bukkit Material associated with the kit
     */
    KitType(
            final String beautifiedName,
            final boolean ranked,
            final Material material
    ) {
        this.bName = beautifiedName;
        this.isRanked = ranked;
        this.material = material;
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
     * @return the display name for the kit
     */
    public String getBeautifiedName() {
        return this.bName;
    }

    /**
     * Indicates whether this kit type is ranked.
     *
     * @return `true` if the kit is ranked, `false` otherwise.
     */
    public boolean isRanked() {
        return this.isRanked;
    }
}