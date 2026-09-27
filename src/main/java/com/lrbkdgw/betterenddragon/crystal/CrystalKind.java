package com.lrbkdgw.betterenddragon.crystal;

/**
 * The four power crystals that are needed to summon the enhanced Ender Dragon.
 */
public enum CrystalKind {
    /** 凋灵水晶 - a pitch black end crystal. */
    WITHER("wither_crystal", 0.16F, 0.15F, 0.18F, false),
    /** 下界水晶 - netherrack coloured end crystal. */
    NETHER("nether_crystal", 0.75F, 0.31F, 0.27F, false),
    /** 深谙水晶 - deep blue end crystal. */
    ABYSSAL("abyssal_crystal", 0.11F, 0.20F, 0.85F, false),
    /** 真理水晶 - an enchanted (glinting) end crystal. */
    TRUTH("truth_crystal", 1.0F, 1.0F, 1.0F, true);

    private final String name;
    private final float red;
    private final float green;
    private final float blue;
    private final boolean glint;

    CrystalKind(String name, float red, float green, float blue, boolean glint) {
        this.name = name;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.glint = glint;
    }

    public String getName() {
        return this.name;
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }

    public boolean glint() {
        return this.glint;
    }
}
