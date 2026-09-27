package com.lrbkdgw.betterenddragon.dragon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * Small helper around the Forge persistent data of an ender dragon.
 */
public final class DragonData {
    public static final String ROOT = "BetterEndDragon";

    public static final String ENHANCED = "Enhanced";
    public static final String STAGE = "Stage";
    public static final String DIVE_COOLDOWN = "DiveCooldown";
    public static final String MITE_TIMER = "MiteTimer";
    public static final String NO_REDUCTION = "NoReduction";
    public static final String UNDYING = "Undying";
    public static final String FINAL_DEATH = "FinalDeath";

    private DragonData() {
    }

    public static CompoundTag get(Entity entity) {
        CompoundTag root = entity.getPersistentData();
        if (!root.contains(ROOT, 10)) {
            root.put(ROOT, new CompoundTag());
        }
        return root.getCompound(ROOT);
    }

    public static boolean isEnhanced(EnderDragon dragon) {
        return get(dragon).getBoolean(ENHANCED);
    }

    public static int stage(EnderDragon dragon) {
        return Math.max(1, get(dragon).getInt(STAGE));
    }
}
