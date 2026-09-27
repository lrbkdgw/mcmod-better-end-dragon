package com.lrbkdgw.betterenddragon.world;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class UnderworldManager {
    public static final String FLIGHT_FLAG = "BetterEndDragonFlight";
    public static final int ENTRY_EXPERIENCE = 500000;

    private UnderworldManager() {
    }

    public static void enterUnderworld(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel underworld = server.getLevel(ModDimensions.UNDERWORLD);
        if (underworld == null) {
            player.sendSystemMessage(Component.translatable("message.betterenddragon.underworld.missing")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        UnderworldBuilder.ensureBuilt(underworld);

        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.removeAllEffects();
        player.clearFire();
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(underworld, 0.5D, ModDimensions.ROAD_Y + 1, 0.5D, -90.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.giveExperiencePoints(ENTRY_EXPERIENCE);

        underworld.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("message.betterenddragon.underworld.welcome")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static void returnToOverworld(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5D, spawn.getY() + 1, spawn.getZ() + 0.5D,
                overworld.getSharedSpawnAngle(), 0.0F);
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
    }

    public static void grantFlight(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(FLIGHT_FLAG, true);
        applyFlight(player);
    }

    public static void applyFlight(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean(FLIGHT_FLAG)) {
            return;
        }
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }
}
