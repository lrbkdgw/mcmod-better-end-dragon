package com.lrbkdgw.betterenddragon.dragon;

import java.util.List;
import java.util.UUID;

import com.lrbkdgw.betterenddragon.netherworld.FakeDeathManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.AABB;

/**
 * All of the empowered dragon behaviour.
 */
public final class EnhancedDragonLogic {

    /** The dragon disappears when there is no player inside this radius. */
    public static final double PLAYER_RANGE = 500.0D;
    /** Radius of the "whole main island" shockwave. */
    public static final double ISLAND_RADIUS = 110.0D;

    public static final int CHARGE_INTERVAL = 100;        // 5 s - much more frequent than vanilla
    public static final int MITE_INTERVAL = 600;          // 30 s
    public static final int CRYSTAL_RESPAWN_DELAY = 200;  // 10 s
    public static final int DR_DISABLE_TICKS = 400;       // 20 s
    public static final int RATTLE_LENGTH = 400;          // 20 s

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("5e2f1f5c-8e1f-4f6c-9d0a-7c1b4f5a2d31");
    private static final String HEALTH_MODIFIER_NAME = "betterenddragon.empowered_health";

    private EnhancedDragonLogic() {
    }

    public static void markEnhanced(EnderDragon dragon) {
        CompoundTag data = DragonState.of(dragon);
        data.putBoolean(DragonState.ENHANCED, true);
        data.putInt(DragonState.STAGE, 1);
    }

    // ------------------------------------------------------------------ tick

    public static void serverTick(EnderDragon dragon) {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = DragonState.of(dragon);
        long time = level.getGameTime();

        if (!data.getBoolean(DragonState.INITIALIZED)) {
            initialise(level, dragon, data);
        }

        if (time % 40L == 0L && level.getNearestPlayer(dragon, PLAYER_RANGE) == null) {
            dragon.discard();
            return;
        }

        if (data.getBoolean(DragonState.FINAL_DEATH)) {
            if (!data.getBoolean(DragonState.FINALE_DONE) && dragon.dragonDeathTime >= 190) {
                data.putBoolean(DragonState.FINALE_DONE, true);
                triggerFinale(level, dragon);
            }
            return;
        }

        if (data.getBoolean(DragonState.IN_RATTLE)) {
            tickDeathRattle(level, dragon, data);
            return;
        }

        if (DragonState.stage(dragon) == 1) {
            tickStageOne(level, dragon, data, time);
        } else {
            tickStageTwo(level, dragon, data, time);
        }
    }

    private static void initialise(ServerLevel level, EnderDragon dragon, CompoundTag data) {
        data.putBoolean(DragonState.INITIALIZED, true);
        applyHealthModifier(dragon);
        dragon.setHealth(dragon.getMaxHealth());
        updateName(dragon, 1);
        placeCages(level);
        broadcast(level, Component.translatable("message.betterenddragon.awaken").withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static void applyHealthModifier(EnderDragon dragon) {
        AttributeInstance attribute = dragon.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(HEALTH_MODIFIER_ID) == null) {
            attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, HEALTH_MODIFIER_NAME,
                    2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static void updateName(EnderDragon dragon, int stage) {
        dragon.setCustomName(Component.translatable(stage == 1
                ? "entity.betterenddragon.empowered_dragon.stage_one"
                : "entity.betterenddragon.empowered_dragon.stage_two"));
        dragon.setCustomNameVisible(false);
    }

    private static void tickStageOne(ServerLevel level, EnderDragon dragon, CompoundTag data, long time) {
        // permanent mining fatigue IV for everybody around
        if (time % 20L == 0L) {
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator() || player.isCreative()) {
                    continue;
                }
                if (player.distanceToSqr(dragon) > PLAYER_RANGE * PLAYER_RANGE) {
                    continue;
                }
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, 3, false, false, true));
            }
        }
        tickChargeAttack(level, dragon, data, time);
    }

    private static void tickStageTwo(ServerLevel level, EnderDragon dragon, CompoundTag data, long time) {
        // crystals never heal the dragon any more
        dragon.nearestCrystal = null;

        if (time % 20L == 0L) {
            dragon.heal(5.0F);
            manageCrystalRespawns(level, data, time);
        }

        tickChargeAttack(level, dragon, data, time);

        long nextMite = data.getLong(DragonState.NEXT_MITE);
        if (nextMite == 0L) {
            data.putLong(DragonState.NEXT_MITE, time + MITE_INTERVAL);
        } else if (time >= nextMite) {
            EnhancedEndermites.summon(level, dragon);
            data.putLong(DragonState.NEXT_MITE, time + MITE_INTERVAL);
        }
    }

    private static void tickChargeAttack(ServerLevel level, EnderDragon dragon, CompoundTag data, long time) {
        long next = data.getLong(DragonState.NEXT_CHARGE);
        if (next == 0L) {
            data.putLong(DragonState.NEXT_CHARGE, time + CHARGE_INTERVAL);
            return;
        }
        if (time < next) {
            return;
        }
        EnderDragonPhase<?> phase = dragon.getPhaseManager().getCurrentPhase().getPhase();
        if (phase != EnderDragonPhase.HOLDING_PATTERN && phase != EnderDragonPhase.STRAFE_PLAYER) {
            data.putLong(DragonState.NEXT_CHARGE, time + 20L);
            return;
        }
        Player target = level.getNearestPlayer(dragon, 180.0D);
        if (target == null) {
            data.putLong(DragonState.NEXT_CHARGE, time + 40L);
            return;
        }
        dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
        dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(target.position());
        data.putLong(DragonState.NEXT_CHARGE, time + CHARGE_INTERVAL);
    }

    private static void tickDeathRattle(ServerLevel level, EnderDragon dragon, CompoundTag data) {
        int remaining = data.getInt(DragonState.RATTLE_TICKS) - 1;
        data.putInt(DragonState.RATTLE_TICKS, remaining);
        if (dragon.getHealth() < 1.0F) {
            dragon.setHealth(1.0F);
        }
        if (remaining % 20 == 0) {
            EnhancedEndermites.summon(level, dragon);
        }
        if (remaining % 80 == 0) {
            islandBlast(level, dragon, 10.0F);
        }
        if (remaining <= 0) {
            data.putBoolean(DragonState.IN_RATTLE, false);
            data.putBoolean(DragonState.FINAL_DEATH, true);
            dragon.setHealth(1.0F);
            dragon.getPhaseManager().setPhase(EnderDragonPhase.DYING);
        }
    }

    // --------------------------------------------------------- transitions

    public static void enterStageTwo(ServerLevel level, EnderDragon dragon) {
        CompoundTag data = DragonState.of(dragon);
        data.putInt(DragonState.STAGE, 2);
        data.putLong(DragonState.NEXT_MITE, level.getGameTime() + MITE_INTERVAL);
        data.putLong(DragonState.DR_DISABLED_UNTIL, 0L);
        data.remove(DragonState.MISSING_CRYSTALS);

        // the iron cages are *not* restored, the crystals are
        clearCages(level);
        restoreAllCrystals(level);

        applyHealthModifier(dragon);
        dragon.setHealth(dragon.getMaxHealth());
        dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        updateName(dragon, 2);

        level.globalLevelEvent(1028, dragon.blockPosition(), 0);
        broadcast(level, Component.translatable("message.betterenddragon.stage_two").withStyle(ChatFormatting.DARK_RED));
    }

    public static void startDeathRattle(ServerLevel level, EnderDragon dragon) {
        CompoundTag data = DragonState.of(dragon);
        data.putBoolean(DragonState.IN_RATTLE, true);
        data.putInt(DragonState.RATTLE_TICKS, RATTLE_LENGTH);
        dragon.setHealth(1.0F);
        level.globalLevelEvent(1028, dragon.blockPosition(), 0);
        broadcast(level, Component.translatable("message.betterenddragon.last_stand").withStyle(ChatFormatting.RED));
    }

    private static void triggerFinale(ServerLevel level, EnderDragon dragon) {
        islandBlast(level, dragon, 0.0F);
        for (int i = 0; i < 8; i++) {
            islandBlastVisuals(level, dragon);
        }
        broadcast(level, Component.translatable("message.betterenddragon.finale").withStyle(ChatFormatting.LIGHT_PURPLE));

        for (ServerPlayer player : List.copyOf(level.players())) {
            if (player.distanceToSqr(dragon) <= PLAYER_RANGE * PLAYER_RANGE) {
                FakeDeathManager.beginFakeDeath(player);
            }
        }
    }

    // ------------------------------------------------------------- helpers

    /** The huge shockwave that covers the whole End main island. */
    public static void islandBlast(ServerLevel level, EnderDragon dragon, float damage) {
        islandBlastVisuals(level, dragon);
        if (damage <= 0.0F) {
            return;
        }
        BlockPos origin = dragon.getFightOrigin();
        DamageSource source = level.damageSources().explosion(dragon, dragon);
        AABB box = new AABB(origin.getX() - ISLAND_RADIUS, level.getMinBuildHeight(), origin.getZ() - ISLAND_RADIUS,
                origin.getX() + ISLAND_RADIUS, level.getMaxBuildHeight(), origin.getZ() + ISLAND_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity instanceof EnderDragon || DragonState.isEnhancedMite(entity)) {
                continue;
            }
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            entity.hurt(source, damage);
        }
    }

    private static void islandBlastVisuals(ServerLevel level, EnderDragon dragon) {
        BlockPos origin = dragon.getFightOrigin();
        for (int i = 0; i < 40; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double radius = level.random.nextDouble() * ISLAND_RADIUS;
            double x = origin.getX() + Math.cos(angle) * radius;
            double z = origin.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, origin.getY() + 64.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, origin.getX(), origin.getY() + 64.0D, origin.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 12.0F, 0.4F);
    }

    public static void broadcast(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
        }
    }

    // ------------------------------------------------------------ crystals

    private static void manageCrystalRespawns(ServerLevel level, CompoundTag data, long time) {
        if (!data.contains(DragonState.MISSING_CRYSTALS, Tag.TAG_COMPOUND)) {
            data.put(DragonState.MISSING_CRYSTALS, new CompoundTag());
        }
        CompoundTag missing = data.getCompound(DragonState.MISSING_CRYSTALS);

        List<SpikeFeature.EndSpike> spikes = SpikeFeature.getSpikesForLevel(level);
        for (int i = 0; i < spikes.size(); i++) {
            SpikeFeature.EndSpike spike = spikes.get(i);
            String key = Integer.toString(i);
            boolean present = !level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).isEmpty();
            if (present) {
                missing.remove(key);
            } else if (!missing.contains(key)) {
                missing.putLong(key, time);
            } else if (time - missing.getLong(key) >= CRYSTAL_RESPAWN_DELAY) {
                spawnSpikeCrystal(level, spike);
                missing.remove(key);
            }
        }
    }

    public static void restoreAllCrystals(ServerLevel level) {
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            if (level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).isEmpty()) {
                spawnSpikeCrystal(level, spike);
            }
        }
    }

    private static void spawnSpikeCrystal(ServerLevel level, SpikeFeature.EndSpike spike) {
        BlockPos base = new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ());
        if (!level.getBlockState(base).is(Blocks.BEDROCK)) {
            level.setBlockAndUpdate(base, Blocks.BEDROCK.defaultBlockState());
        }
        EndCrystal crystal = EntityType.END_CRYSTAL.create(level);
        if (crystal == null) {
            return;
        }
        crystal.setBeamTarget(null);
        crystal.moveTo(spike.getCenterX() + 0.5D, spike.getHeight() + 1, spike.getCenterZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(crystal);
        level.levelEvent(3000, crystal.blockPosition(), 0);
    }

    /** Recreates the vanilla "guarded pillar" iron bar cage on every single pillar. */
    public static void placeCages(ServerLevel level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        boolean edgeX = Mth.abs(dx) == 2;
                        boolean edgeZ = Mth.abs(dz) == 2;
                        boolean top = dy == 3;
                        if (!edgeX && !edgeZ && !top) {
                            continue;
                        }
                        boolean nsAxis = dx == -2 || dx == 2 || top;
                        boolean weAxis = dz == -2 || dz == 2 || top;
                        BlockState bars = Blocks.IRON_BARS.defaultBlockState()
                                .setValue(IronBarsBlock.NORTH, nsAxis && dz != -2)
                                .setValue(IronBarsBlock.SOUTH, nsAxis && dz != 2)
                                .setValue(IronBarsBlock.WEST, weAxis && dx != -2)
                                .setValue(IronBarsBlock.EAST, weAxis && dx != 2);
                        pos.set(spike.getCenterX() + dx, spike.getHeight() + dy, spike.getCenterZ() + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, bars, 2);
                        }
                    }
                }
            }
        }
    }

    /** Removes whatever is left of the cages - stage two never restores them. */
    public static void clearCages(ServerLevel level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        pos.set(spike.getCenterX() + dx, spike.getHeight() + dy, spike.getCenterZ() + dz);
                        if (level.getBlockState(pos).is(Blocks.IRON_BARS)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }
}
