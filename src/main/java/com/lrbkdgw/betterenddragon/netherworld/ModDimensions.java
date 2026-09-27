package com.lrbkdgw.betterenddragon.netherworld;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ModDimensions {

    /** 冥界 - the Netherworld. */
    public static final ResourceKey<Level> NETHERWORLD =
            ResourceKey.create(Registries.DIMENSION, BetterEndDragon.id("netherworld"));

    private ModDimensions() {
    }
}
