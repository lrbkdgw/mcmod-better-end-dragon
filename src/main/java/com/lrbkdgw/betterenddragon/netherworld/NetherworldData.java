package com.lrbkdgw.betterenddragon.netherworld;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class NetherworldData extends SavedData {

    private static final String FILE_ID = "betterenddragon_netherworld";
    private static final String TAG_GENERATED = "Generated";

    private boolean generated;

    public static NetherworldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(NetherworldData::load, NetherworldData::new, FILE_ID);
    }

    public static NetherworldData load(CompoundTag tag) {
        NetherworldData data = new NetherworldData();
        data.generated = tag.getBoolean(TAG_GENERATED);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean(TAG_GENERATED, generated);
        return tag;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
        setDirty();
    }
}
