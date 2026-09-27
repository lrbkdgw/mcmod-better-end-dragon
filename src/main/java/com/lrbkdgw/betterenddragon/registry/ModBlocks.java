package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.block.RitualDeviceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BetterEndDragon.MOD_ID);

    public static final RegistryObject<Block> KEEP_INVENTORY_DEVICE = BLOCKS.register("keep_inventory_device",
            () -> new RitualDeviceBlock(deviceProperties(), RitualDeviceBlock.Action.KEEP_INVENTORY));
    public static final RegistryObject<Block> CREATIVE_FLIGHT_DEVICE = BLOCKS.register("creative_flight_device",
            () -> new RitualDeviceBlock(deviceProperties(), RitualDeviceBlock.Action.CREATIVE_FLIGHT));
    public static final RegistryObject<Block> RETURN_DEVICE = BLOCKS.register("return_device",
            () -> new RitualDeviceBlock(deviceProperties(), RitualDeviceBlock.Action.RETURN_HOME));

    public static final RegistryObject<Item> KEEP_INVENTORY_DEVICE_ITEM =
            ModItems.ITEMS.register("keep_inventory_device", () -> blockItem(KEEP_INVENTORY_DEVICE));
    public static final RegistryObject<Item> CREATIVE_FLIGHT_DEVICE_ITEM =
            ModItems.ITEMS.register("creative_flight_device", () -> blockItem(CREATIVE_FLIGHT_DEVICE));
    public static final RegistryObject<Item> RETURN_DEVICE_ITEM =
            ModItems.ITEMS.register("return_device", () -> blockItem(RETURN_DEVICE));

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties deviceProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(-1.0F, 3600000.0F)
                .lightLevel(state -> 15)
                .sound(SoundType.AMETHYST)
                .noLootTable();
    }

    private static BlockItem blockItem(RegistryObject<Block> block) {
        return new BlockItem(block.get(), new Item.Properties().rarity(Rarity.EPIC));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
