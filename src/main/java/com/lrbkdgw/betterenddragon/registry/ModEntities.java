package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.crystal.CrystalKind;
import com.lrbkdgw.betterenddragon.crystal.PowerCrystalEntity;
import com.lrbkdgw.betterenddragon.entity.AbyssalEndermite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = BetterEndDragon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BetterEndDragon.MOD_ID);

    public static final RegistryObject<EntityType<PowerCrystalEntity>> WITHER_CRYSTAL =
            registerCrystal(CrystalKind.WITHER);
    public static final RegistryObject<EntityType<PowerCrystalEntity>> NETHER_CRYSTAL =
            registerCrystal(CrystalKind.NETHER);
    public static final RegistryObject<EntityType<PowerCrystalEntity>> ABYSSAL_CRYSTAL =
            registerCrystal(CrystalKind.ABYSSAL);
    public static final RegistryObject<EntityType<PowerCrystalEntity>> TRUTH_CRYSTAL =
            registerCrystal(CrystalKind.TRUTH);

    public static final RegistryObject<EntityType<AbyssalEndermite>> ABYSSAL_ENDERMITE =
            ENTITIES.register("abyssal_endermite", () -> EntityType.Builder
                    .<AbyssalEndermite>of(AbyssalEndermite::new, MobCategory.MONSTER)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(10)
                    .build("abyssal_endermite"));

    private ModEntities() {
    }

    private static RegistryObject<EntityType<PowerCrystalEntity>> registerCrystal(CrystalKind kind) {
        return ENTITIES.register(kind.getName(), () -> EntityType.Builder
                .<PowerCrystalEntity>of((type, level) -> new PowerCrystalEntity(type, level, kind), MobCategory.MISC)
                .sized(2.0F, 2.0F)
                .clientTrackingRange(16)
                .updateInterval(Integer.MAX_VALUE)
                .build(kind.getName()));
    }

    public static RegistryObject<EntityType<PowerCrystalEntity>> byKind(CrystalKind kind) {
        switch (kind) {
            case WITHER:
                return WITHER_CRYSTAL;
            case NETHER:
                return NETHER_CRYSTAL;
            case ABYSSAL:
                return ABYSSAL_CRYSTAL;
            case TRUTH:
            default:
                return TRUTH_CRYSTAL;
        }
    }

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ABYSSAL_ENDERMITE.get(), AbyssalEndermite.createEnhancedAttributes().build());
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
