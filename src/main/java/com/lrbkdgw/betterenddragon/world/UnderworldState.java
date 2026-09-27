package com.lrbkdgw.betterenddragon.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class UnderworldState extends SavedData {
    public static final String NAME = "betterenddragon_underworld";

    public boolean built;

    public static UnderworldState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(UnderworldState::load, UnderworldState::new, NAME);
    }

    public static UnderworldState load(CompoundTag tag) {
        UnderworldState state = new UnderworldState();
        state.built = tag.getBoolean("Built");
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Built", this.built);
        return tag;
    }
}
