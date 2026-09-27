package com.lrbkdgw.betterenddragon.client;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterEndDragon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WITHER_CRYSTAL.get(), PowerCrystalRenderer::new);
        event.registerEntityRenderer(ModEntities.NETHER_CRYSTAL.get(), PowerCrystalRenderer::new);
        event.registerEntityRenderer(ModEntities.ABYSSAL_CRYSTAL.get(), PowerCrystalRenderer::new);
        event.registerEntityRenderer(ModEntities.TRUTH_CRYSTAL.get(), PowerCrystalRenderer::new);
        event.registerEntityRenderer(ModEntities.ABYSSAL_ENDERMITE.get(), AbyssalEndermiteRenderer::new);
    }
}
