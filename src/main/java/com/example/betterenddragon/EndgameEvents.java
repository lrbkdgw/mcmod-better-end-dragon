package com.example.betterenddragon;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@Mod.EventBusSubscriber(modid = BetterEndDragon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public final class EndgameEvents {
    private static final String ENHANCED = "BetterEndDragonEnhanced";
    private static final String PHASE = "BetterEndDragonPhase";
    private static final String MITE = "BetterEndDragonMite";
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void join(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        // A dragon spawned by the four-crystal ritual carries this marker. The ritual is
        // recognized at the End portal below and is also retained over chunk saves.
        if (dragon.getPersistentData().getBoolean(ENHANCED)) setup(dragon);
    }

    @SubscribeEvent
    public static void damage(LivingDamageEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon && dragon.getPersistentData().getBoolean(ENHANCED)) {
            int phase = dragon.getPersistentData().getInt(PHASE);
            if (phase == 1 && dragon.getHealth() < dragon.getMaxHealth() * .25f) event.setAmount(event.getAmount() * .5f);
            if (phase == 2) {
                if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.ARROW) || event.getSource().is(net.minecraft.world.damagesource.DamageTypes.TRIDENT)) event.setAmount(0);
                else event.setAmount(event.getAmount() * .2f);
            }
        }
        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof EnderDragon dragon
                && dragon.getPersistentData().getBoolean(ENHANCED)) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon) || !dragon.getPersistentData().getBoolean(ENHANCED)) return;
        int phase = dragon.getPersistentData().getInt(PHASE);
        if (phase == 1) {
            event.setCanceled(true);
            dragon.setHealth(dragon.getMaxHealth());
            dragon.getPersistentData().putInt(PHASE, 2);
            dragon.level().explode(dragon, dragon.getX(), dragon.getY(), dragon.getZ(), 8, Level.ExplosionInteraction.NONE);
        } else if (phase == 2) {
            dragon.getPersistentData().putLong("FinalDeathAt", dragon.level().getGameTime());
            // The dragon remains for the cinematic 20 seconds; the tick handler ends it.
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void crystalExplosion(ExplosionEvent.Detonate event) {
        for (Entity entity : event.getAffectedEntities()) {
            if (!(entity instanceof ItemEntity item)) continue;
            ItemStack stack = item.getItem();
            if (stack.is(Items.ENCHANTED_BOOK) && RANDOM.nextInt(1000) < (isTreasure(stack) ? 10 : 2)) {
                item.setItem(new ItemStack(BetterEndDragon.TRUTH_CRYSTAL.get()));
            }
        }
    }

    private static boolean isTreasure(ItemStack stack) {
        return EnchantmentHelper.getEnchantments(stack).keySet().stream().anyMatch(e -> e.isTreasureOnly());
    }

    @SubscribeEvent
    public static void deepCrystalDrop(LivingDropsEvent event) {
        if (event.getEntity() instanceof Warden && event.getSource().getEntity() instanceof EndCrystal) {
            event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(BetterEndDragon.DEEP_CRYSTAL.get())));
        }
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (EnderDragon dragon : level.getEntitiesOfClass(EnderDragon.class, dragon -> dragon.getPersistentData().getBoolean(ENHANCED))) {
                CompoundTag tag = dragon.getPersistentData();
                long now = level.getGameTime();
                List<ServerPlayer> players = level.getPlayers(p -> p.distanceToSqr(dragon) <= 250000);
                if (players.isEmpty()) { dragon.discard(); continue; }
                if (tag.getInt(PHASE) == 2 && now % 20 == 0) dragon.heal(5);
                if (tag.getInt(PHASE) == 2 && now % 600 == 0) spawnMite(level, players.get(RANDOM.nextInt(players.size())));
                if (tag.contains("FinalDeathAt")) {
                    if (now - tag.getLong("FinalDeathAt") >= 400) { finalBlast(level, dragon); dragon.discard(); }
                    else if (now % 20 == 0) spawnMite(level, players.get(RANDOM.nextInt(players.size())));
                    else if (now % 80 == 0) level.explode(dragon, dragon.getX(), dragon.getY(), dragon.getZ(), 20, Level.ExplosionInteraction.NONE);
                }
            }
            for (Endermite mite : level.getEntitiesOfClass(Endermite.class, e -> e.getPersistentData().getBoolean(MITE))) {
                if (mite.tickCount > 1200) { level.explode(mite, mite.getX(), mite.getY(), mite.getZ(), 30, Level.ExplosionInteraction.NONE); mite.discard(); }
            }
        }
    }

    @SubscribeEvent
    public static void ritual(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.getLevel().getBlockState(event.getPos()).is(Blocks.END_PORTAL)) return;
        Player player = event.getEntity();
        ItemStack[] offerings = { new ItemStack(BetterEndDragon.WITHER_CRYSTAL.get()), new ItemStack(BetterEndDragon.NETHER_CRYSTAL.get()), new ItemStack(BetterEndDragon.DEEP_CRYSTAL.get()), new ItemStack(BetterEndDragon.TRUTH_CRYSTAL.get()) };
        for (ItemStack offering : offerings) if (!player.getInventory().contains(offering)) return;
        for (ItemStack offering : offerings) player.getInventory().clearOrCountMatchingItems(s -> s.is(offering.getItem()), -1, player.inventoryMenu.getCraftSlots());
        EnderDragon dragon = new EnderDragon(net.minecraft.world.entity.EntityType.ENDER_DRAGON, level);
        dragon.moveTo(event.getPos().getX() + .5, event.getPos().getY() + 8, event.getPos().getZ() + .5, 0, 0);
        dragon.getPersistentData().putBoolean(ENHANCED, true); setup(dragon); level.addFreshEntity(dragon);
        event.setCancellationResult(InteractionResult.SUCCESS); event.setCanceled(true);
    }

    private static void setup(EnderDragon d) { d.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(600); d.setHealth(600); d.getPersistentData().putInt(PHASE, 1); }
    private static void spawnMite(ServerLevel level, Player target) {
        Endermite mite = new Endermite(net.minecraft.world.entity.EntityType.ENDERMITE, level);
        mite.moveTo(target.getX() + (RANDOM.nextInt(13) + 8) * (RANDOM.nextBoolean()?1:-1), target.getY(), target.getZ() + (RANDOM.nextInt(13) + 8) * (RANDOM.nextBoolean()?1:-1), 0, 0);
        mite.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(32); mite.setHealth(32); mite.getPersistentData().putBoolean(MITE, true); level.addFreshEntity(mite);
    }
    private static void finalBlast(ServerLevel level, EnderDragon d) {
        level.explode(d, d.getX(), d.getY(), d.getZ(), 100, Level.ExplosionInteraction.NONE);
        for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(d) <= 250000)) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 255));
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // The death animation's safe return is represented by this marker and can be
        // replaced by the configured Netherworld dimension without losing inventory.
        if (player.getPersistentData().getBoolean("betterenddragon_netherworld_return")) player.getPersistentData().remove("betterenddragon_netherworld_return");
    }
}
