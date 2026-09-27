package com.lrbkdgw.betterenddragon.netherworld;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Keeps the "creative flight" reward across deaths, respawns and dimension changes.
 */
@Mod.EventBusSubscriber(modid = BetterEndDragon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerFlight {

    private static final String TAG_FLIGHT = "BetterEndDragonFlight";

    private PlayerFlight() {
    }

    public static void grant(ServerPlayer player) {
        persistent(player).putBoolean(TAG_FLIGHT, true);
        apply(player);
    }

    public static void apply(ServerPlayer player) {
        if (!persistent(player).getBoolean(TAG_FLIGHT)) {
            return;
        }
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    private static CompoundTag persistent(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            apply(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            apply(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            apply(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag oldTag = persistent(event.getOriginal());
        if (oldTag.getBoolean(TAG_FLIGHT)) {
            persistent(event.getEntity()).putBoolean(TAG_FLIGHT, true);
        }
    }
}
