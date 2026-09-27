package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.crystal.SpecialEndCrystal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BetterEndDragon.MODID);

    public static final RegistryObject<EntityType<SpecialEndCrystal>> SPECIAL_END_CRYSTAL =
            ENTITIES.register("special_end_crystal", () -> EntityType.Builder
                    .<SpecialEndCrystal>of(SpecialEndCrystal::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(16)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(BetterEndDragon.MODID + ":special_end_crystal"));

    private ModEntities() {
    }
}
