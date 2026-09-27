package com.lrbkdgw.betterenddragon.netherworld;

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
 * The "device" on top of the three special Netherworld beacon towers.
 */
public class NetherworldDeviceBlock extends Block {

    public enum Kind {
        KEEP_INVENTORY,
        CREATIVE_FLIGHT,
        RETURN_HOME
    }

    private final Kind kind;

    public NetherworldDeviceBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);

        switch (kind) {
            case KEEP_INVENTORY -> {
                MinecraftServer server = serverPlayer.server;
                server.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);
                for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                    other.sendSystemMessage(Component.translatable("message.betterenddragon.keep_inventory_on")
                            .withStyle(ChatFormatting.GOLD));
                }
            }
            case CREATIVE_FLIGHT -> {
                PlayerFlight.grant(serverPlayer);
                serverPlayer.sendSystemMessage(Component.translatable("message.betterenddragon.flight_granted")
                        .withStyle(ChatFormatting.AQUA));
            }
            case RETURN_HOME -> NetherworldTravel.returnToOverworld(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }
}
