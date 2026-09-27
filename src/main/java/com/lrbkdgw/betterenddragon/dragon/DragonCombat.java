package com.lrbkdgw.betterenddragon.dragon;

import com.lrbkdgw.betterenddragon.entity.AbyssalEndermite;
import com.lrbkdgw.betterenddragon.registry.ModEntities;
import com.lrbkdgw.betterenddragon.state.EndFightState;
import com.lrbkdgw.betterenddragon.world.FakeDeathManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

import java.util.List;

/**
 * All of the behaviour of the enhanced ender dragon.
 */
public final class DragonCombat {
    /** 300% of the vanilla 200 health. */
    public static final double ENHANCED_HEALTH = 600.0D;
    public static final float DAMAGE_MULTIPLIER = 1.5F;
    public static final double DESPAWN_RANGE = 500.0D;
    public static final double MAIN_ISLAND_RADIUS = 120.0D;
    public static final int DIVE_INTERVAL = 100;
    public static final int MITE_INTERVAL = 600;
    public static final int UNDYING_TIME = 400;
    public static final int CRYSTAL_RESPAWN_TIME = 200;

    private DragonCombat() {
    }

    /* ------------------------------------------------------------------ */
    /* setup                                                               */
    /* ------------------------------------------------------------------ */

    public static void applyStats(EnderDragon dragon) {
        AttributeInstance health = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getBaseValue() != ENHANCED_HEALTH) {
            health.setBaseValue(ENHANCED_HEALTH);
        }
    }

    public static void makeEnhanced(ServerLevel level, EnderDragon dragon) {
        CompoundTag data = DragonData.get(dragon);
        data.putBoolean(DragonData.ENHANCED, true);
        data.putInt(DragonData.STAGE, 1);
        data.putInt(DragonData.DIVE_COOLDOWN, DIVE_INTERVAL);
        data.putInt(DragonData.MITE_TIMER, MITE_INTERVAL);
        data.putInt(DragonData.NO_REDUCTION, 0);
        data.putInt(DragonData.UNDYING, 0);
        data.putBoolean(DragonData.FINAL_DEATH, false);

        applyStats(dragon);
        dragon.setHealth(dragon.getMaxHealth());
        dragon.setCustomName(Component.translatable("entity.betterenddragon.enhanced_ender_dragon"));

        // every crystal gets an iron cage during phase one
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            if (level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).isEmpty()) {
                spawnSpikeCrystal(level, spike);
            }
            cageSpike(level, spike);
        }

        broadcast(level, Component.translatable("message.betterenddragon.stage1").withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static void enterStage2(ServerLevel level, EnderDragon dragon) {
        CompoundTag data = DragonData.get(dragon);
        data.putInt(DragonData.STAGE, 2);
        data.putInt(DragonData.MITE_TIMER, MITE_INTERVAL);
        data.putInt(DragonData.NO_REDUCTION, 0);
        data.putInt(DragonData.UNDYING, 0);

        applyStats(dragon);
        dragon.setHealth(dragon.getMaxHealth());
        dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);

        // restore every crystal, but not the iron cages
        EndFightState state = EndFightState.get(level);
        for (int i = 0; i < state.crystalRespawn.length; i++) {
            state.crystalRespawn[i] = 0;
        }
        state.setDirty();
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            if (level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).isEmpty()) {
                spawnSpikeCrystal(level, spike);
            }
        }

        level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 6.0F, 0.5F);
        broadcast(level, Component.translatable("message.betterenddragon.stage2").withStyle(ChatFormatting.DARK_RED));
    }

    /* ------------------------------------------------------------------ */
    /* per tick behaviour                                                  */
    /* ------------------------------------------------------------------ */

    public static void tickDragon(EnderDragon dragon) {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = DragonData.get(dragon);
        if (!data.getBoolean(DragonData.ENHANCED)) {
            return;
        }

        int stage = Math.max(1, data.getInt(DragonData.STAGE));

        if (dragon.tickCount % 20 == 0 && level.getNearestPlayer(dragon, DESPAWN_RANGE) == null) {
            // no player around: vanish, but stay armed so the fight brings the
            // empowered dragon back instead of a normal one
            EndFightState.get(level).arm();
            dragon.discard();
            return;
        }

        int noReduction = data.getInt(DragonData.NO_REDUCTION);
        if (noReduction > 0) {
            data.putInt(DragonData.NO_REDUCTION, noReduction - 1);
        }

        if (stage == 1) {
            tickStageOne(level, dragon, data);
        } else {
            tickStageTwo(level, dragon, data);
        }
    }

    private static void tickStageOne(ServerLevel level, EnderDragon dragon, CompoundTag data) {
        if (dragon.tickCount % 20 == 0) {
            for (ServerPlayer player : level.players()) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 3, false, false, true));
                }
            }
        }

        int cooldown = data.getInt(DragonData.DIVE_COOLDOWN);
        if (cooldown > 0) {
            data.putInt(DragonData.DIVE_COOLDOWN, cooldown - 1);
            return;
        }

        if (dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.HOLDING_PATTERN) {
            Player target = level.getNearestPlayer(dragon, 250.0D);
            if (target != null) {
                dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                DragonChargePlayerPhase phase = dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER);
                phase.setTarget(target.position());
                data.putInt(DragonData.DIVE_COOLDOWN, DIVE_INTERVAL);
            }
        }
    }

    private static void tickStageTwo(ServerLevel level, EnderDragon dragon, CompoundTag data) {
        // end crystals never heal the dragon again
        dragon.nearestCrystal = null;

        int undying = data.getInt(DragonData.UNDYING);
        if (undying > 0) {
            dragon.setHealth(1.0F);
            if (undying % 20 == 0) {
                summonEndermite(level, dragon);
            }
            if (undying % 80 == 0) {
                islandBlast(level, 10.0F);
            }
            undying--;
            data.putInt(DragonData.UNDYING, undying);
            if (undying <= 0) {
                beginFinalDeath(level, dragon);
            }
            return;
        }

        if (dragon.tickCount % 20 == 0) {
            dragon.heal(5.0F);
        }

        int mite = data.getInt(DragonData.MITE_TIMER) - 1;
        if (mite <= 0) {
            summonEndermite(level, dragon);
            mite = MITE_INTERVAL;
        }
        data.putInt(DragonData.MITE_TIMER, mite);
    }

    /**
     * Level wide upkeep: ritual timeout, crystal respawn and the final blast.
     */
    public static void tickLevel(ServerLevel level) {
        EndFightState state = EndFightState.get(level);

        if (state.pendingTicks > 0) {
            state.pendingTicks--;
            if (state.pendingTicks <= 0) {
                state.pendingEnhanced = false;
            }
            state.setDirty();
        }

        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        if (state.finalDeathPending) {
            Entity dragon = state.dyingDragon == null ? null : level.getEntity(state.dyingDragon);
            if (dragon == null || dragon.isRemoved()) {
                state.finalDeathPending = false;
                state.dyingDragon = null;
                state.setDirty();
                finalExplosion(level);
            }
            return;
        }

        EnderDragon dragon = findEnhancedDragon(level);
        if (dragon == null || DragonData.stage(dragon) < 2) {
            return;
        }

        List<SpikeFeature.EndSpike> spikes = SpikeFeature.getSpikesForLevel(level);
        for (int i = 0; i < spikes.size() && i < state.crystalRespawn.length; i++) {
            SpikeFeature.EndSpike spike = spikes.get(i);
            boolean alive = !level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).isEmpty();
            if (alive) {
                if (state.crystalRespawn[i] != 0) {
                    state.crystalRespawn[i] = 0;
                    state.setDirty();
                }
                continue;
            }
            if (state.crystalRespawn[i] <= 0) {
                state.crystalRespawn[i] = CRYSTAL_RESPAWN_TIME;
            } else {
                state.crystalRespawn[i] -= 20;
                if (state.crystalRespawn[i] <= 0) {
                    state.crystalRespawn[i] = 0;
                    spawnSpikeCrystal(level, spike);
                }
            }
            state.setDirty();
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                             */
    /* ------------------------------------------------------------------ */

    public static EnderDragon findEnhancedDragon(ServerLevel level) {
        for (EnderDragon dragon : level.getEntities(EntityType.ENDER_DRAGON, d -> d.isAlive() && DragonData.isEnhanced(d))) {
            return dragon;
        }
        return null;
    }

    public static boolean isLandingPhase(EnderDragon dragon) {
        EnderDragonPhase<?> phase = dragon.getPhaseManager().getCurrentPhase().getPhase();
        return phase == EnderDragonPhase.LANDING
                || phase == EnderDragonPhase.LANDING_APPROACH
                || phase == EnderDragonPhase.SITTING_ATTACKING
                || phase == EnderDragonPhase.SITTING_FLAMING
                || phase == EnderDragonPhase.SITTING_SCANNING;
    }

    public static boolean isChargingPhase(EnderDragon dragon) {
        return dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.CHARGING_PLAYER;
    }

    public static void spawnSpikeCrystal(ServerLevel level, SpikeFeature.EndSpike spike) {
        EndCrystal crystal = EntityType.END_CRYSTAL.create(level);
        if (crystal == null) {
            return;
        }
        crystal.setBeamTarget(null);
        crystal.moveTo(spike.getCenterX() + 0.5D, spike.getHeight() + 1, spike.getCenterZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(crystal);
        level.setBlockAndUpdate(new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ()),
                Blocks.BEDROCK.defaultBlockState());
    }

    public static void cageSpike(ServerLevel level, SpikeFeature.EndSpike spike) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    boolean edgeX = Math.abs(dx) == 2;
                    boolean edgeZ = Math.abs(dz) == 2;
                    boolean top = dy == 3;
                    if (!edgeX && !edgeZ && !top) {
                        continue;
                    }
                    boolean connectZ = edgeX || top;
                    boolean connectX = edgeZ || top;
                    BlockState bars = Blocks.IRON_BARS.defaultBlockState()
                            .setValue(IronBarsBlock.NORTH, connectZ && dz != -2)
                            .setValue(IronBarsBlock.SOUTH, connectZ && dz != 2)
                            .setValue(IronBarsBlock.WEST, connectX && dx != -2)
                            .setValue(IronBarsBlock.EAST, connectX && dx != 2);
                    level.setBlock(new BlockPos(spike.getCenterX() + dx, spike.getHeight() + dy, spike.getCenterZ() + dz),
                            bars, 2);
                }
            }
        }
    }

    public static void summonEndermite(ServerLevel level, EnderDragon dragon) {
        List<ServerPlayer> players = level.players().stream()
                .filter(p -> p.isAlive() && !p.isSpectator() && !p.isCreative())
                .toList();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = 8.0D + level.random.nextDouble() * 12.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.getY() + 1.0D;

        AbyssalEndermite mite = ModEntities.ABYSSAL_ENDERMITE.get().create(level);
        if (mite == null) {
            return;
        }
        mite.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        mite.setPersistenceRequired();
        mite.setTarget(player);
        level.addFreshEntity(mite);
        level.sendParticles(ParticleTypes.PORTAL, x, y, z, 40, 0.6D, 0.6D, 0.6D, 0.4D);
        level.playSound(null, mite.blockPosition(), SoundEvents.ENDERMITE_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    public static void absorbEndermite(ServerLevel level, AbyssalEndermite mite) {
        mite.discard();
        level.sendParticles(ParticleTypes.DRAGON_BREATH, mite.getX(), mite.getY() + 0.5D, mite.getZ(),
                60, 0.5D, 0.5D, 0.5D, 0.3D);
        islandBlast(level, 50.0F);
    }

    /** A blast that covers the whole main End island. */
    public static void islandBlast(ServerLevel level, float damage) {
        level.playSound(null, BlockPos.ZERO, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 10.0F, 0.4F);
        for (int i = 0; i < 24; i++) {
            double x = (level.random.nextDouble() - 0.5D) * 2.0D * MAIN_ISLAND_RADIUS;
            double z = (level.random.nextDouble() - 0.5D) * 2.0D * MAIN_ISLAND_RADIUS;
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, 68.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }
            if (horizontalDistanceToCenter(player) <= MAIN_ISLAND_RADIUS) {
                player.hurt(level.damageSources().explosion(null, null), damage);
            }
        }
    }

    private static double horizontalDistanceToCenter(Entity entity) {
        return Math.sqrt(entity.getX() * entity.getX() + entity.getZ() * entity.getZ());
    }

    public static void startUndying(ServerLevel level, EnderDragon dragon) {
        CompoundTag data = DragonData.get(dragon);
        data.putInt(DragonData.UNDYING, UNDYING_TIME);
        dragon.setHealth(1.0F);
        level.playSound(null, dragon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 8.0F, 0.3F);
        broadcast(level, Component.translatable("message.betterenddragon.undying").withStyle(ChatFormatting.RED));
    }

    public static void beginFinalDeath(ServerLevel level, EnderDragon dragon) {
        CompoundTag data = DragonData.get(dragon);
        data.putBoolean(DragonData.FINAL_DEATH, true);
        data.putInt(DragonData.UNDYING, 0);
        dragon.setHealth(1.0F);
        dragon.getPhaseManager().setPhase(EnderDragonPhase.DYING);

        EndFightState state = EndFightState.get(level);
        state.dyingDragon = dragon.getUUID();
        state.finalDeathPending = true;
        state.setDirty();
    }

    /** The extremely violent blast that drags every nearby player into the Underworld. */
    public static void finalExplosion(ServerLevel level) {
        level.playSound(null, BlockPos.ZERO, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 20.0F, 0.2F);
        for (int i = 0; i < 64; i++) {
            double x = (level.random.nextDouble() - 0.5D) * 2.0D * MAIN_ISLAND_RADIUS;
            double z = (level.random.nextDouble() - 0.5D) * 2.0D * MAIN_ISLAND_RADIUS;
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, 64.0D + level.random.nextDouble() * 20.0D, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        FakeDeathManager.triggerNear(level, DESPAWN_RANGE);
    }

    public static void broadcast(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
        }
    }

    public static void applyDiveEffects(EnderDragon dragon, LivingEntity victim) {
        if (victim instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
            dragon.heal(dragon.getMaxHealth() * 0.1F);
        }
    }
}
