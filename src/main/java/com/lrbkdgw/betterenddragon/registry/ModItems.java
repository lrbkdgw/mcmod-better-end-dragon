package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.crystal.CrystalKind;
import com.lrbkdgw.betterenddragon.crystal.PowerCrystalItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterEndDragon.MOD_ID);

    public static final RegistryObject<Item> WITHER_CRYSTAL = ITEMS.register(CrystalKind.WITHER.getName(),
            () -> new PowerCrystalItem(new Item.Properties().rarity(Rarity.EPIC), CrystalKind.WITHER));
    public static final RegistryObject<Item> NETHER_CRYSTAL = ITEMS.register(CrystalKind.NETHER.getName(),
            () -> new PowerCrystalItem(new Item.Properties().rarity(Rarity.EPIC), CrystalKind.NETHER));
    public static final RegistryObject<Item> ABYSSAL_CRYSTAL = ITEMS.register(CrystalKind.ABYSSAL.getName(),
            () -> new PowerCrystalItem(new Item.Properties().rarity(Rarity.EPIC), CrystalKind.ABYSSAL));
    public static final RegistryObject<Item> TRUTH_CRYSTAL = ITEMS.register(CrystalKind.TRUTH.getName(),
            () -> new PowerCrystalItem(new Item.Properties().rarity(Rarity.EPIC), CrystalKind.TRUTH));

    private ModItems() {
    }

    public static RegistryObject<Item> byKind(CrystalKind kind) {
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

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
