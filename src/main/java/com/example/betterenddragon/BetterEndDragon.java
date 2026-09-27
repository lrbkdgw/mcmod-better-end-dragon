package com.example.betterenddragon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.ForgeRegistries;

@Mod(BetterEndDragon.MOD_ID)
public final class BetterEndDragon {
    public static final String MOD_ID = "betterenddragon";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredHolder<Item, Item> WITHER_CRYSTAL = ITEMS.register("wither_crystal", () -> new CrystalItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NETHER_CRYSTAL = ITEMS.register("nether_crystal", () -> new CrystalItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> DEEP_CRYSTAL = ITEMS.register("deep_crystal", () -> new CrystalItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> TRUTH_CRYSTAL = ITEMS.register("truth_crystal", () -> new CrystalItem(new Item.Properties()));

    public BetterEndDragon(IEventBus bus) { ITEMS.register(bus); }
    public static ResourceLocation id(String path) { return new ResourceLocation(MOD_ID, path); }
}
