package club.revived.lobby.game.duel;

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
            "Netherite Potion",
            false,
            Material.NETHERITE_CHESTPLATE
    ),

    DIAMOND_POTION(
            "Diamond Potion",
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

    KitType(
            final String beautifiedName,
            final boolean ranked,
            final Material material
    ) {
        this.bName = beautifiedName;
        this.isRanked = ranked;
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public String getBeautifiedName() {
        return this.bName;
    }

    public boolean isRanked() {
        return this.isRanked;
    }
}
