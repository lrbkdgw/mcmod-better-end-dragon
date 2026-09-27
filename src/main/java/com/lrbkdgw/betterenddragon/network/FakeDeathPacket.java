package com.lrbkdgw.betterenddragon.network;

import com.lrbkdgw.betterenddragon.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -> client: show the (fake) death screen.
 */
public class FakeDeathPacket {
    public FakeDeathPacket() {
    }

    public static void encode(FakeDeathPacket packet, FriendlyByteBuf buffer) {
    }

    public static FakeDeathPacket decode(FriendlyByteBuf buffer) {
        return new FakeDeathPacket();
    }

    public static void handle(FakeDeathPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openFakeDeathScreen));
        ctx.setPacketHandled(true);
    }
}
