package com.lrbkdgw.betterenddragon.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerEvents {
    private PlayerEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UnderworldManager.applyFlight(player);
        if (FakeDeathManager.isAwaiting(player)) {
            FakeDeathManager.trigger(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UnderworldManager.applyFlight(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UnderworldManager.applyFlight(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getOriginal().getPersistentData().getBoolean(UnderworldManager.FLIGHT_FLAG)) {
            player.getPersistentData().putBoolean(UnderworldManager.FLIGHT_FLAG, true);
        }
        if (event.getOriginal().getPersistentData().getBoolean(FakeDeathManager.AWAITING_FLAG)) {
            player.getPersistentData().putBoolean(FakeDeathManager.AWAITING_FLAG, true);
        }
    }
}
