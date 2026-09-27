package com.lrbkdgw.betterenddragon;

import com.lrbkdgw.betterenddragon.dragon.DragonEvents;
import com.lrbkdgw.betterenddragon.network.ModNetwork;
import com.lrbkdgw.betterenddragon.registry.ModBlocks;
import com.lrbkdgw.betterenddragon.registry.ModCreativeTabs;
import com.lrbkdgw.betterenddragon.registry.ModEntities;
import com.lrbkdgw.betterenddragon.registry.ModItems;
import com.lrbkdgw.betterenddragon.world.PlayerEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BetterEndDragon.MOD_ID)
public class BetterEndDragon {
    public static final String MOD_ID = "betterenddragon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterEndDragon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModEntities.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(DragonEvents.class);
        MinecraftForge.EVENT_BUS.register(PlayerEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
