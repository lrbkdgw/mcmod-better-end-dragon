package com.lrbkdgw.betterenddragon.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

/**
 * Per End-dimension state of the enhanced dragon fight.
 */
public class EndFightState extends SavedData {
    public static final String NAME = "betterenddragon_fight";
    public static final int CRYSTAL_SLOTS = 10;

    /** Set when the four power crystals were placed on the portal. */
    public boolean pendingEnhanced;
    /** Countdown (ticks) for the armed ritual. */
    public int pendingTicks;
    /** Per obsidian pillar respawn countdown used in phase two. */
    public int[] crystalRespawn = new int[CRYSTAL_SLOTS];
    /** Dragon that is playing its final death animation. */
    public UUID dyingDragon;
    public boolean finalDeathPending;

    public static EndFightState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(EndFightState::load, EndFightState::new, NAME);
    }

    public static EndFightState load(CompoundTag tag) {
        EndFightState state = new EndFightState();
        state.pendingEnhanced = tag.getBoolean("PendingEnhanced");
        state.pendingTicks = tag.getInt("PendingTicks");
        int[] stored = tag.getIntArray("CrystalRespawn");
        if (stored.length == CRYSTAL_SLOTS) {
            state.crystalRespawn = stored;
        }
        if (tag.hasUUID("DyingDragon")) {
            state.dyingDragon = tag.getUUID("DyingDragon");
        }
        state.finalDeathPending = tag.getBoolean("FinalDeathPending");
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("PendingEnhanced", this.pendingEnhanced);
        tag.putInt("PendingTicks", this.pendingTicks);
        tag.putIntArray("CrystalRespawn", this.crystalRespawn);
        if (this.dyingDragon != null) {
            tag.putUUID("DyingDragon", this.dyingDragon);
        }
        tag.putBoolean("FinalDeathPending", this.finalDeathPending);
        return tag;
    }

    public void arm() {
        this.pendingEnhanced = true;
        this.pendingTicks = 6000;
        this.setDirty();
    }

    public void disarm() {
        this.pendingEnhanced = false;
        this.pendingTicks = 0;
        this.setDirty();
    }
}
