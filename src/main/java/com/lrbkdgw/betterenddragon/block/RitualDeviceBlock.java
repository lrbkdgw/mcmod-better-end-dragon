package com.lrbkdgw.betterenddragon.block;

import com.lrbkdgw.betterenddragon.world.UnderworldManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The device on top of the three special Underworld beacons.
 */
public class RitualDeviceBlock extends Block {
    public enum Action {
        KEEP_INVENTORY,
        CREATIVE_FLIGHT,
        RETURN_HOME
    }

    private final Action action;

    public RitualDeviceBlock(Properties properties, Action action) {
        super(properties);
        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        MinecraftServer server = serverPlayer.server;
        switch (this.action) {
            case KEEP_INVENTORY -> {
                server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);
                serverPlayer.sendSystemMessage(Component.translatable("message.betterenddragon.device.keep_inventory")
                        .withStyle(ChatFormatting.GOLD));
            }
            case CREATIVE_FLIGHT -> {
                UnderworldManager.grantFlight(serverPlayer);
                serverPlayer.sendSystemMessage(Component.translatable("message.betterenddragon.device.flight")
                        .withStyle(ChatFormatting.AQUA));
            }
            case RETURN_HOME -> {
                serverPlayer.sendSystemMessage(Component.translatable("message.betterenddragon.device.return")
                        .withStyle(ChatFormatting.GREEN));
                UnderworldManager.returnToOverworld(serverPlayer);
            }
        }

        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.2F);
        return InteractionResult.CONSUME;
    }
}
