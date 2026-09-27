package com.lrbkdgw.betterenddragon.world;

import com.lrbkdgw.betterenddragon.network.FakeDeathPacket;
import com.lrbkdgw.betterenddragon.network.ModNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * The "death animation" that is not a real death: no items are dropped and a
 * hardcore world is not ended by it. Players wake up in the Underworld.
 */
public final class FakeDeathManager {
    public static final String AWAITING_FLAG = "BetterEndDragonAwaitingUnderworld";

    private FakeDeathManager() {
    }

    public static void triggerNear(ServerLevel level, double radius) {
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            if (player.distanceToSqr(Vec3.ZERO) > radiusSqr) {
                continue;
            }
            trigger(player);
        }
    }

    public static void trigger(ServerPlayer player) {
        player.getPersistentData().putBoolean(AWAITING_FLAG, true);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_BIG_FALL,
                SoundSource.PLAYERS, 1.0F, 0.6F);
        ModNetwork.sendToPlayer(new FakeDeathPacket(), player);
    }

    public static boolean isAwaiting(ServerPlayer player) {
        return player.getPersistentData().getBoolean(AWAITING_FLAG);
    }

    public static void clear(ServerPlayer player) {
        player.getPersistentData().putBoolean(AWAITING_FLAG, false);
    }
}
