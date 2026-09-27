package com.lrbkdgw.betterenddragon.dragon;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterEndDragon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DragonEvents {

    private DragonEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (entity instanceof EnderDragon dragon) {
            if (DragonState.isEnhanced(dragon)) {
                EnhancedDragonLogic.serverTick(dragon);
            }
        } else if (entity instanceof Endermite mite) {
            if (DragonState.isEnhancedMite(mite)) {
                EnhancedEndermites.serverTick(mite);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel level) || level.dimension() != Level.END) {
            return;
        }
        if (level.getGameTime() % 10L == 0L) {
            RitualTracker.scan(level);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        RitualTracker.clear();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getEntity() instanceof EnderDragon dragon) || DragonState.isEnhanced(dragon)) {
            return;
        }
        if (RitualTracker.consume(level)) {
            EnhancedDragonLogic.markEnhanced(dragon);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || !(target instanceof EnderDragon dragon)) {
            return;
        }
        if (!DragonState.isEnhanced(dragon)) {
            return;
        }
        CompoundTag data = DragonState.of(dragon);
        if (data.getBoolean(DragonState.IN_RATTLE) || data.getBoolean(DragonState.FINAL_DEATH)) {
            event.setCanceled(true);
            return;
        }
        // stage two is completely immune to projectiles
        if (DragonState.stage(dragon) == 2 && event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        // ----- damage dealt by the empowered dragon: 150 %, plus the dive-bomb rider
        if (attacker instanceof EnderDragon dragon && DragonState.isEnhanced(dragon)) {
            event.setAmount(event.getAmount() * 1.5F);
            if (target instanceof Player player
                    && dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.CHARGING_PLAYER) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                dragon.heal(dragon.getMaxHealth() * 0.10F);
            }
        }

        // ----- damage dealt by an empowered endermite
        if (attacker instanceof Endermite mite && DragonState.isEnhancedMite(mite) && target instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
        }

        // ----- damage taken by the empowered dragon
        if (target instanceof EnderDragon dragon && DragonState.isEnhanced(dragon)) {
            CompoundTag data = DragonState.of(dragon);
            float amount = event.getAmount();
            if (DragonState.stage(dragon) == 1) {
                EnderDragonPhase<?> phase = dragon.getPhaseManager().getCurrentPhase().getPhase();
                if (dragon.getPhaseManager().getCurrentPhase().isSitting()
                        || phase == EnderDragonPhase.LANDING
                        || phase == EnderDragonPhase.LANDING_APPROACH) {
                    amount *= 0.5F;
                }
            } else if (dragon.level().getGameTime() >= data.getLong(DragonState.DR_DISABLED_UNTIL)) {
                amount *= 0.2F;
            }
            event.setAmount(amount);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) {
            return;
        }
        if (!(dragon.level() instanceof ServerLevel level) || !DragonState.isEnhanced(dragon)) {
            return;
        }
        CompoundTag data = DragonState.of(dragon);
        if (data.getBoolean(DragonState.IN_RATTLE) || data.getBoolean(DragonState.FINAL_DEATH)) {
            event.setCanceled(true);
            return;
        }
        if (event.getAmount() < dragon.getHealth()) {
            return;
        }
        event.setCanceled(true);
        if (DragonState.stage(dragon) == 1) {
            EnhancedDragonLogic.enterStageTwo(level, dragon);
        } else {
            EnhancedDragonLogic.startDeathRattle(level, dragon);
        }
    }
}
