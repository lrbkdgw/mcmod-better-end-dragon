package com.lrbkdgw.betterenddragon.dragon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

/**
 * Thin accessor around the Forge persistent data of an entity. All empowered-dragon state lives in
 * a single {@code BetterEndDragon} compound so that it survives save/load and chunk unloads.
 */
public final class DragonState {

    public static final String ROOT = "BetterEndDragon";

    public static final String ENHANCED = "Enhanced";
    public static final String INITIALIZED = "Initialized";
    public static final String STAGE = "Stage";
    public static final String NEXT_CHARGE = "NextCharge";
    public static final String NEXT_MITE = "NextMite";
    public static final String NEXT_REGEN = "NextRegen";
    public static final String DR_DISABLED_UNTIL = "DrDisabledUntil";
    public static final String RATTLE_TICKS = "RattleTicks";
    public static final String IN_RATTLE = "InRattle";
    public static final String FINAL_DEATH = "FinalDeath";
    public static final String FINALE_DONE = "FinaleDone";
    public static final String MISSING_CRYSTALS = "MissingCrystals";

    /** Marker for empowered endermites. */
    public static final String MITE_MARKER = "EnhancedMite";
    public static final String MITE_SPAWN_TIME = "MiteSpawnTime";

    private DragonState() {
    }

    public static CompoundTag of(Entity entity) {
        CompoundTag root = entity.getPersistentData();
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) {
            root.put(ROOT, new CompoundTag());
        }
        return root.getCompound(ROOT);
    }

    public static boolean isEnhanced(Entity entity) {
        CompoundTag root = entity.getPersistentData();
        return root.contains(ROOT, Tag.TAG_COMPOUND) && root.getCompound(ROOT).getBoolean(ENHANCED);
    }

    public static int stage(Entity entity) {
        CompoundTag data = of(entity);
        int stage = data.getInt(STAGE);
        return stage <= 0 ? 1 : stage;
    }

    public static boolean isEnhancedMite(Entity entity) {
        CompoundTag root = entity.getPersistentData();
        return root.contains(ROOT, Tag.TAG_COMPOUND) && root.getCompound(ROOT).getBoolean(MITE_MARKER);
    }
}
