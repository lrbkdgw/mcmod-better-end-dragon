package com.lrbkdgw.betterenddragon;

import com.lrbkdgw.betterenddragon.registry.ModBlocks;
import com.lrbkdgw.betterenddragon.registry.ModCreativeTabs;
import com.lrbkdgw.betterenddragon.registry.ModEntities;
import com.lrbkdgw.betterenddragon.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Better End Dragon.
 *
 * <p>Adds four special end crystals ({@code wither}, {@code nether}, {@code deep}, {@code truth}),
 * a two-phase empowered Ender Dragon that is summoned when all four are used for the respawn ritual,
 * and the "Netherworld" reward dimension that is entered through the dragon's final death blast.</p>
 */
@Mod(BetterEndDragon.MODID)
public class BetterEndDragon {

    public static final String MODID = "betterenddragon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterEndDragon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.BLOCK_ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
