package com.lrbkdgw.betterenddragon.world;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {
    public static final ResourceLocation UNDERWORLD_ID = new ResourceLocation(BetterEndDragon.MOD_ID, "underworld");
    public static final ResourceKey<Level> UNDERWORLD = ResourceKey.create(Registries.DIMENSION, UNDERWORLD_ID);

    /** Length of the single road of the Underworld. */
    public static final int ROAD_LENGTH = 500;
    /** Y level of the road surface. */
    public static final int ROAD_Y = 64;
    /** Distance between two reward stations. */
    public static final int STATION_SPACING = 50;

    private ModDimensions() {
    }
}
