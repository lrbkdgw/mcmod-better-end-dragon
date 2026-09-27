package com.lrbkdgw.betterenddragon.crystal;

import com.lrbkdgw.betterenddragon.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An end crystal variant. It extends the vanilla end crystal so that the vanilla
 * dragon respawn ritual ({@code EndDragonFight#tryRespawn}) accepts it.
 */
public class PowerCrystalEntity extends EndCrystal {
    private final CrystalKind kind;

    public PowerCrystalEntity(EntityType<? extends EndCrystal> type, Level level, CrystalKind kind) {
        super(type, level);
        this.kind = kind;
    }

    public CrystalKind getKind() {
        return this.kind;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.byKind(this.kind).get());
    }
}
