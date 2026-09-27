package com.lrbkdgw.betterenddragon;

import com.lrbkdgw.betterenddragon.dragon.DragonCombat;
import com.lrbkdgw.betterenddragon.dragon.DragonData;
import com.lrbkdgw.betterenddragon.registry.ModItems;
import com.lrbkdgw.betterenddragon.state.EndFightState;
import com.lrbkdgw.betterenddragon.world.FakeDeathManager;
import com.lrbkdgw.betterenddragon.world.UnderworldManager;
import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Small operator toolbox, mostly useful to test the different stages.
 */
public final class ModCommands {
    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("betterenddragon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("crystals").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.WITHER_CRYSTAL.get()));
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.NETHER_CRYSTAL.get()));
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.ABYSSAL_CRYSTAL.get()));
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.TRUTH_CRYSTAL.get()));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("arm").executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    EndFightState.get(level).arm();
                    context.getSource().sendSuccess(() ->
                            Component.literal("The next dragon respawn will be empowered."), true);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("empower").executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    EnderDragon dragon = firstDragon(level);
                    if (dragon == null) {
                        context.getSource().sendFailure(Component.literal("No ender dragon in this dimension."));
                        return 0;
                    }
                    DragonCombat.makeEnhanced(level, dragon);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("stage2").executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    EnderDragon dragon = firstDragon(level);
                    if (dragon == null || !DragonData.isEnhanced(dragon)) {
                        context.getSource().sendFailure(Component.literal("No empowered ender dragon here."));
                        return 0;
                    }
                    DragonCombat.enterStage2(level, dragon);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("fakedeath").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    FakeDeathManager.trigger(player);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("underworld").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    UnderworldManager.enterUnderworld(player);
                    return Command.SINGLE_SUCCESS;
                })));
    }

    private static EnderDragon firstDragon(ServerLevel level) {
        for (EnderDragon dragon : level.getEntities(EntityType.ENDER_DRAGON, d -> d.isAlive())) {
            return dragon;
        }
        return null;
    }
}
