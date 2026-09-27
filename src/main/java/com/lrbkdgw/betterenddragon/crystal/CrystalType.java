package com.lrbkdgw.betterenddragon.crystal;

/**
 * The four special end crystal variants added by this mod.
 *
 * <p>Every variant is rendered with the vanilla end crystal model, just tinted (and, for the
 * truth crystal, covered in an enchantment glint).</p>
 */
public enum CrystalType {
    /** 凋灵水晶 - a blackened end crystal. */
    WITHER("wither_crystal", 0.17F, 0.15F, 0.20F, false),
    /** 下界水晶 - netherrack coloured. */
    NETHER("nether_crystal", 0.80F, 0.31F, 0.26F, false),
    /** 深谙水晶 - deep blue. */
    DEEP("deep_crystal", 0.12F, 0.26F, 0.88F, false),
    /** 真理水晶 - regular colours plus an enchantment glint. */
    TRUTH("truth_crystal", 1.0F, 1.0F, 1.0F, true);

    private static final CrystalType[] VALUES = values();

    private final String itemName;
    private final float red;
    private final float green;
    private final float blue;
    private final boolean glint;

    CrystalType(String itemName, float red, float green, float blue, boolean glint) {
        this.itemName = itemName;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.glint = glint;
    }

    public String itemName() {
        return itemName;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public boolean glint() {
        return glint;
    }

    public static CrystalType byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return WITHER;
        }
        return VALUES[id];
    }
}
