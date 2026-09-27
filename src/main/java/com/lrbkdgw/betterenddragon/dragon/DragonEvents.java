package com.lrbkdgw.betterenddragon.dragon;

import com.lrbkdgw.betterenddragon.registry.ModItems;
import com.lrbkdgw.betterenddragon.state.EndFightState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class DragonEvents {
    private static final double TRUTH_CHANCE = 0.002D;
    private static final double TRUTH_CHANCE_TREASURE = 0.01D;

    private DragonEvents() {
    }

    /* ------------------------------------------------------------------ */
    /* summoning                                                           */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getEntity() instanceof EnderDragon dragon)) {
            return;
        }
        if (DragonData.isEnhanced(dragon)) {
            DragonCombat.applyStats(dragon);
            return;
        }
        EndFightState state = EndFightState.get(level);
        if (state.pendingEnhanced) {
            state.disarm();
            DragonCombat.makeEnhanced(level, dragon);
        }
    }

    /* ------------------------------------------------------------------ */
    /* ticking                                                             */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon && !dragon.level().isClientSide()) {
            DragonCombat.tickDragon(dragon);
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
        DragonCombat.tickLevel(level);
    }

    /* ------------------------------------------------------------------ */
    /* combat                                                              */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon) || dragon.level().isClientSide()) {
            return;
        }
        if (!DragonData.isEnhanced(dragon)) {
            return;
        }
        if (DragonData.stage(dragon) >= 2 && event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();

        Entity attacker = source.getEntity();
        if (attacker instanceof EnderDragon dragon && DragonData.isEnhanced(dragon)) {
            event.setAmount(event.getAmount() * DragonCombat.DAMAGE_MULTIPLIER);
            if (DragonData.stage(dragon) == 1 && DragonCombat.isChargingPhase(dragon)) {
                DragonCombat.applyDiveEffects(dragon, victim);
            }
        }

        if (victim instanceof EnderDragon dragon && DragonData.isEnhanced(dragon)) {
            CompoundTag data = DragonData.get(dragon);
            int stage = Math.max(1, data.getInt(DragonData.STAGE));

            if (source.is(DamageTypeTags.IS_EXPLOSION) && source.getDirectEntity() instanceof EndCrystal) {
                data.putInt(DragonData.NO_REDUCTION, 400);
            }

            float multiplier = 1.0F;
            if (stage == 1) {
                if (DragonCombat.isLandingPhase(dragon)) {
                    multiplier *= 0.5F;
                }
            } else if (data.getInt(DragonData.NO_REDUCTION) <= 0) {
                multiplier *= 0.2F;
            }
            if (multiplier != 1.0F) {
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) {
            return;
        }
        if (!(dragon.level() instanceof ServerLevel level) || !DragonData.isEnhanced(dragon)) {
            return;
        }
        CompoundTag data = DragonData.get(dragon);
        if (data.getBoolean(DragonData.FINAL_DEATH)) {
            return;
        }
        if (event.getAmount() < dragon.getHealth()) {
            return;
        }

        event.setCanceled(true);
        int stage = Math.max(1, data.getInt(DragonData.STAGE));
        if (stage == 1) {
            dragon.setHealth(1.0F);
            DragonCombat.enterStage2(level, dragon);
        } else if (data.getInt(DragonData.UNDYING) <= 0) {
            DragonCombat.startUndying(level, dragon);
        } else {
            dragon.setHealth(1.0F);
        }
    }

    /* ------------------------------------------------------------------ */
    /* crystal driven drops                                                */
    /* ------------------------------------------------------------------ */

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity instanceof Warden)) {
            return;
        }
        if (!(event.getSource().getDirectEntity() instanceof EndCrystal)) {
            return;
        }
        ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                new ItemStack(ModItems.ABYSSAL_CRYSTAL.get()));
        drop.setDefaultPickUpDelay();
        entity.level().addFreshEntity(drop);
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        if (!(event.getExplosion().getDirectSourceEntity() instanceof EndCrystal)) {
            return;
        }

        List<Entity> affected = new ArrayList<>(event.getAffectedEntities());
        for (Entity entity : affected) {
            if (!(entity instanceof ItemEntity itemEntity)) {
                continue;
            }
            ItemStack stack = itemEntity.getItem();
            if (!stack.is(Items.ENCHANTED_BOOK)) {
                continue;
            }
            double chance = hasTreasureEnchantment(stack) ? TRUTH_CHANCE_TREASURE : TRUTH_CHANCE;
            int converted = 0;
            for (int i = 0; i < stack.getCount(); i++) {
                if (level.random.nextDouble() < chance) {
                    converted++;
                }
            }
            if (converted <= 0) {
                continue;
            }
            stack.shrink(converted);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
            ItemEntity drop = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                    new ItemStack(ModItems.TRUTH_CRYSTAL.get(), converted));
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
    }

    private static boolean hasTreasureEnchantment(ItemStack stack) {
        ListTag list = EnchantedBookItem.getEnchantments(stack);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            ResourceLocation id = EnchantmentHelper.getEnchantmentId(tag);
            if (id == null) {
                continue;
            }
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
            if (enchantment != null && enchantment.isTreasureOnly()) {
                return true;
            }
        }
        return false;
    }
}
