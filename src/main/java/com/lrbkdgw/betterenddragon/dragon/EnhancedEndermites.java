package com.lrbkdgw.betterenddragon.dragon;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The empowered endermites summoned by the stage-two dragon.
 */
public final class EnhancedEndermites {

    /** 60 seconds - after that the dragon absorbs the mite. */
    public static final int LIFETIME = 1200;

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("9a3d4fd1-cb0f-4f0b-9f2a-1e0f5b7d3c22");
    private static final UUID FOLLOW_MODIFIER_ID = UUID.fromString("27f1b0a5-6d0e-4e0c-8b6d-3a5c9e1f7b44");

    private EnhancedEndermites() {
    }

    public static void summon(ServerLevel level, EnderDragon dragon) {
        Player player = level.getNearestPlayer(dragon, EnhancedDragonLogic.PLAYER_RANGE);
        if (player == null) {
            return;
        }

        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = 8.0D + level.random.nextDouble() * 12.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BlockPos.containing(x, 0.0D, z));
        double y = ground.getY() > level.getMinBuildHeight() + 1 ? ground.getY() : player.getY();

        Endermite mite = EntityType.ENDERMITE.create(level);
        if (mite == null) {
            return;
        }
        mite.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        mite.setPersistenceRequired();
        mite.setCustomName(Component.translatable("entity.betterenddragon.empowered_endermite"));
        mite.setCustomNameVisible(false);

        CompoundTag data = DragonState.of(mite);
        data.putBoolean(DragonState.MITE_MARKER, true);
        data.putLong(DragonState.MITE_SPAWN_TIME, level.getGameTime());

        AttributeInstance health = mite.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_MODIFIER_ID) == null) {
            health.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID,
                    "betterenddragon.mite_health", 7.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        AttributeInstance follow = mite.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null && follow.getModifier(FOLLOW_MODIFIER_ID) == null) {
            follow.addPermanentModifier(new AttributeModifier(FOLLOW_MODIFIER_ID,
                    "betterenddragon.mite_follow", 256.0D, AttributeModifier.Operation.ADDITION));
        }
        mite.setHealth(mite.getMaxHealth());
        mite.setTarget(player);

        level.addFreshEntity(mite);
        level.sendParticles(ParticleTypes.PORTAL, x, y + 0.5D, z, 40, 0.5D, 0.5D, 0.5D, 0.2D);
    }

    public static void serverTick(Endermite mite) {
        if (!(mite.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = DragonState.of(mite);

        // only ever interested in players
        LivingEntity target = mite.getTarget();
        if (!(target instanceof Player) || !target.isAlive()) {
            mite.setTarget(level.getNearestPlayer(mite, 256.0D));
        }
        mite.setPersistenceRequired();

        long spawned = data.getLong(DragonState.MITE_SPAWN_TIME);
        if (level.getGameTime() - spawned < LIFETIME) {
            return;
        }

        EnderDragon dragon = findDragon(level);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, mite.getX(), mite.getY() + 0.5D, mite.getZ(),
                60, 0.5D, 0.5D, 0.5D, 0.1D);
        mite.discard();
        if (dragon != null && DragonState.isEnhanced(dragon)) {
            EnhancedDragonLogic.islandBlast(level, dragon, 50.0F);
        }
    }

    public static EnderDragon findDragon(ServerLevel level) {
        EndDragonFight fight = level.getDragonFight();
        if (fight == null || fight.getDragonUUID() == null) {
            return null;
        }
        Entity entity = level.getEntity(fight.getDragonUUID());
        return entity instanceof EnderDragon dragon ? dragon : null;
    }
}
