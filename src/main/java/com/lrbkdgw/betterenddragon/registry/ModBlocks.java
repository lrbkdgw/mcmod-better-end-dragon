package com.lrbkdgw.betterenddragon.registry;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.netherworld.NetherworldDeviceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BetterEndDragon.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterEndDragon.MODID);

    public static final RegistryObject<Block> KEEP_INVENTORY_DEVICE =
            device("keep_inventory_device", NetherworldDeviceBlock.Kind.KEEP_INVENTORY);
    public static final RegistryObject<Block> CREATIVE_FLIGHT_DEVICE =
            device("creative_flight_device", NetherworldDeviceBlock.Kind.CREATIVE_FLIGHT);
    public static final RegistryObject<Block> RETURN_HOME_DEVICE =
            device("return_home_device", NetherworldDeviceBlock.Kind.RETURN_HOME);

    private ModBlocks() {
    }

    private static RegistryObject<Block> device(String name, NetherworldDeviceBlock.Kind kind) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new NetherworldDeviceBlock(kind,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK)
                        .strength(-1.0F, 3600000.0F)
                        .lightLevel(state -> 15)
                        .sound(SoundType.AMETHYST)
                        .noLootTable()));
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().rarity(Rarity.EPIC)));
        return block;
    }
}
