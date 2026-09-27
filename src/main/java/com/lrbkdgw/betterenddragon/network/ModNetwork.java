package com.lrbkdgw.betterenddragon.network;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static SimpleChannel CHANNEL;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(BetterEndDragon.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        int id = 0;
        CHANNEL.registerMessage(id++, FakeDeathPacket.class,
                FakeDeathPacket::encode, FakeDeathPacket::decode, FakeDeathPacket::handle);
        CHANNEL.registerMessage(id++, UnderworldRespawnPacket.class,
                UnderworldRespawnPacket::encode, UnderworldRespawnPacket::decode, UnderworldRespawnPacket::handle);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        if (CHANNEL != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    public static void sendToServer(Object message) {
        if (CHANNEL != null) {
            CHANNEL.sendToServer(message);
        }
    }
}
