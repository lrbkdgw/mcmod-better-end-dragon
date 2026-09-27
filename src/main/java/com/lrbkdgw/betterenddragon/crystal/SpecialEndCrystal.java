package com.lrbkdgw.betterenddragon.crystal;

import com.lrbkdgw.betterenddragon.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An end crystal variant. It extends the vanilla {@link EndCrystal} on purpose: every piece of
 * vanilla logic that looks for crystals (the dragon fight respawn ritual, the dragon's crystal
 * healing scan, ...) uses {@code getEntitiesOfClass(EndCrystal.class, ...)} and therefore picks
 * subclasses up for free.
 */
public class SpecialEndCrystal extends EndCrystal {

    private static final EntityDataAccessor<Integer> DATA_CRYSTAL_TYPE =
            SynchedEntityData.defineId(SpecialEndCrystal.class, EntityDataSerializers.INT);

    private static final String TAG_CRYSTAL_TYPE = "CrystalType";

    public SpecialEndCrystal(EntityType<? extends SpecialEndCrystal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CRYSTAL_TYPE, 0);
    }

    public CrystalType getCrystalType() {
        return CrystalType.byId(this.entityData.get(DATA_CRYSTAL_TYPE));
    }

    public void setCrystalType(CrystalType type) {
        this.entityData.set(DATA_CRYSTAL_TYPE, type.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_CRYSTAL_TYPE, this.entityData.get(DATA_CRYSTAL_TYPE));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_CRYSTAL_TYPE)) {
            this.entityData.set(DATA_CRYSTAL_TYPE, tag.getInt(TAG_CRYSTAL_TYPE));
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.crystalItem(getCrystalType()));
    }
}
