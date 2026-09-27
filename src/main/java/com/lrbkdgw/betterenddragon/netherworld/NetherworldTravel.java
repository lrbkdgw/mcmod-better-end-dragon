package com.lrbkdgw.betterenddragon.netherworld;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class NetherworldTravel {

    /** 进入冥界后，玩家固定获得 500000 点经验。 */
    public static final int ENTRY_EXPERIENCE = 500_000;

    private NetherworldTravel() {
    }

    public static void sendToNetherworld(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel target = server.getLevel(ModDimensions.NETHERWORLD);
        if (target == null) {
            player.sendSystemMessage(Component.literal("[Better End Dragon] The Netherworld dimension is missing.")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        NetherworldGenerator.ensureGenerated(target);

        player.teleportTo(target, NetherworldGenerator.SPAWN_X, NetherworldGenerator.SPAWN_Y,
                NetherworldGenerator.SPAWN_Z, -90.0F, 0.0F);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.clearFire();
        player.removeAllEffects();
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.giveExperiencePoints(ENTRY_EXPERIENCE);
        PlayerFlight.apply(player);
        player.sendSystemMessage(Component.translatable("message.betterenddragon.netherworld_welcome")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static void returnToOverworld(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                overworld.getSharedSpawnAngle(), 0.0F);
        player.sendSystemMessage(Component.translatable("message.betterenddragon.returned_home")
                .withStyle(ChatFormatting.GREEN));
    }
}
