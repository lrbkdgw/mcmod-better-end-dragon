package com.lrbkdgw.betterenddragon.network;

import com.lrbkdgw.betterenddragon.world.FakeDeathManager;
import com.lrbkdgw.betterenddragon.world.UnderworldManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: the player pressed "respawn" on the fake death screen.
 */
public class UnderworldRespawnPacket {
    public UnderworldRespawnPacket() {
    }

    public static void encode(UnderworldRespawnPacket packet, FriendlyByteBuf buffer) {
    }

    public static UnderworldRespawnPacket decode(FriendlyByteBuf buffer) {
        return new UnderworldRespawnPacket();
    }

    public static void handle(UnderworldRespawnPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !FakeDeathManager.isAwaiting(player)) {
                return;
            }
            FakeDeathManager.clear(player);
            UnderworldManager.enterUnderworld(player);
        });
        ctx.setPacketHandled(true);
    }
}
