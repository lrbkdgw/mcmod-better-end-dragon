package com.lrbkdgw.betterenddragon.client;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterEndDragon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPECIAL_END_CRYSTAL.get(), SpecialEndCrystalRenderer::new);
    }
}
