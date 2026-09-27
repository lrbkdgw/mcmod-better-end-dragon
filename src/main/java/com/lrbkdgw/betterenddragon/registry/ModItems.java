package com.lrbkdgw.betterenddragon.registry;

import java.util.EnumMap;
import java.util.Map;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.crystal.CrystalType;
import com.lrbkdgw.betterenddragon.crystal.SpecialEndCrystalItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterEndDragon.MODID);

    private static final Map<CrystalType, RegistryObject<Item>> CRYSTALS = new EnumMap<>(CrystalType.class);

    public static final RegistryObject<Item> WITHER_CRYSTAL = crystal(CrystalType.WITHER);
    public static final RegistryObject<Item> NETHER_CRYSTAL = crystal(CrystalType.NETHER);
    public static final RegistryObject<Item> DEEP_CRYSTAL = crystal(CrystalType.DEEP);
    public static final RegistryObject<Item> TRUTH_CRYSTAL = crystal(CrystalType.TRUTH);

    private ModItems() {
    }

    private static RegistryObject<Item> crystal(CrystalType type) {
        RegistryObject<Item> item = ITEMS.register(type.itemName(),
                () -> new SpecialEndCrystalItem(type, new Item.Properties().rarity(Rarity.EPIC)));
        CRYSTALS.put(type, item);
        return item;
    }

    public static Item crystalItem(CrystalType type) {
        RegistryObject<Item> object = CRYSTALS.get(type);
        return object == null ? WITHER_CRYSTAL.get() : object.get();
    }
}
