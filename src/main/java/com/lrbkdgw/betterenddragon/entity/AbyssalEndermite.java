package com.lrbkdgw.betterenddragon.entity;

import com.lrbkdgw.betterenddragon.dragon.DragonCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

/**
 * 强化后的末影螨: 800% health, only ever hostile towards players and absorbed by
 * the dragon (with a huge blast) if it survives for a minute.
 */
public class AbyssalEndermite extends Endermite {
    private static final int ABSORB_TIME = 1200;

    private int absorbTimer;

    public AbyssalEndermite(EntityType<? extends Endermite> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createEnhancedAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 256.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = this.getTarget();
        if (target != null && !(target instanceof Player)) {
            this.setTarget(null);
        }
        if (target == null || !target.isAlive()) {
            Player player = this.level().getNearestPlayer(this, 256.0D);
            if (player != null && player.isAlive() && !player.isSpectator() && !player.isCreative()) {
                this.setTarget(player);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            this.absorbTimer++;
            if (this.absorbTimer >= ABSORB_TIME) {
                DragonCombat.absorbEndermite(serverLevel, this);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
        }
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AbsorbTimer", this.absorbTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.absorbTimer = tag.getInt("AbsorbTimer");
    }
}
