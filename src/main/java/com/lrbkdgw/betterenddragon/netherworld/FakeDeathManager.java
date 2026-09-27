package com.lrbkdgw.betterenddragon.netherworld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * The "死亡动画" the empowered dragon forces on everybody when it finally dies.
 *
 * <p>The server never actually kills anybody: it simply sends the vanilla combat-kill packet, which
 * makes the client show the death screen. Because the player is still alive nothing is dropped, and
 * hardcore worlds are not ended. A few seconds later the player is moved to the Netherworld, which
 * sends a respawn packet and therefore replaces the death screen client side.</p>
 */
@Mod.EventBusSubscriber(modid = BetterEndDragon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FakeDeathManager {

    /** How long the fake death screen stays up before the player wakes up in the Netherworld. */
    private static final int FAKE_DEATH_TICKS = 100;

    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    private FakeDeathManager() {
    }

    public static void beginFakeDeath(ServerPlayer player) {
        if (PENDING.containsKey(player.getUUID())) {
            return;
        }
        PENDING.put(player.getUUID(), FAKE_DEATH_TICKS);

        player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(),
                Component.translatable("death.attack.betterenddragon.dragon_finale", player.getDisplayName())));

        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                FAKE_DEATH_TICKS + 60, 255, false, false, false));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            int remaining = entry.getValue() - 1;
            if (remaining > 0) {
                entry.setValue(remaining);
                player.setDeltaMovement(0.0D, 0.0D, 0.0D);
                continue;
            }
            iterator.remove();
            NetherworldTravel.sendToNetherworld(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
