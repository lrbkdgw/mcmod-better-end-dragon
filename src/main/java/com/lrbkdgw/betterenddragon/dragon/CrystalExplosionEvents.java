package com.lrbkdgw.betterenddragon.dragon;

import java.util.ArrayList;
import java.util.List;

import com.lrbkdgw.betterenddragon.BetterEndDragon;
import com.lrbkdgw.betterenddragon.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Everything that reacts to an end-crystal explosion:
 * <ul>
 *     <li>Wardens killed by one drop a deep crystal.</li>
 *     <li>Destroyed enchanted books may turn into a truth crystal.</li>
 *     <li>The empowered dragon loses its stage-two damage reduction for 20 seconds.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = BetterEndDragon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CrystalExplosionEvents {

    private static final double NORMAL_BOOK_CHANCE = 0.002D;   // 0.2 %
    private static final double TREASURE_BOOK_CHANCE = 0.01D;  // 1 %

    private CrystalExplosionEvents() {
    }

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity exploder = event.getExplosion().getDirectSourceEntity();
        if (!(exploder instanceof EndCrystal crystal)) {
            return;
        }

        List<Entity> affected = new ArrayList<>(event.getAffectedEntities());
        for (Entity entity : affected) {
            if (entity instanceof ItemEntity itemEntity) {
                rollTruthCrystal(serverLevel, itemEntity);
            }
        }

        applyToEmpoweredDragon(serverLevel, crystal, event.getExplosion().getDamageSource());
    }

    private static void applyToEmpoweredDragon(ServerLevel level, EndCrystal crystal, DamageSource explosionSource) {
        List<EnderDragon> dragons = level.getEntitiesOfClass(EnderDragon.class,
                crystal.getBoundingBox().inflate(16.0D));
        for (EnderDragon dragon : dragons) {
            if (!DragonState.isEnhanced(dragon)) {
                continue;
            }
            CompoundTag data = DragonState.of(dragon);
            data.putLong(DragonState.DR_DISABLED_UNTIL,
                    level.getGameTime() + EnhancedDragonLogic.DR_DISABLE_TICKS);

            // Vanilla ignores any dragon damage that is not attributed to a player, so a crystal
            // blast would normally do nothing at all. Re-attribute it to the closest player.
            if (explosionSource != null && explosionSource.getEntity() != null) {
                continue; // the vanilla explosion already hits the dragon
            }
            Player attacker = level.getNearestPlayer(crystal, 256.0D);
            if (attacker == null) {
                continue;
            }
            dragon.hurt(dragon.head, level.damageSources().explosion(crystal, attacker), 30.0F);
        }
    }

    private static void rollTruthCrystal(ServerLevel level, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!stack.is(Items.ENCHANTED_BOOK) || stack.isEmpty()) {
            return;
        }
        double chance = isTreasure(stack) ? TREASURE_BOOK_CHANCE : NORMAL_BOOK_CHANCE;
        int converted = 0;
        for (int i = 0; i < stack.getCount(); i++) {
            if (level.random.nextDouble() < chance) {
                converted++;
            }
        }
        if (converted == 0) {
            return;
        }
        stack.shrink(converted);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
        ItemEntity drop = new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(),
                new ItemStack(ModItems.TRUTH_CRYSTAL.get(), converted));
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }

    private static boolean isTreasure(ItemStack stack) {
        ListTag enchantments = EnchantedBookItem.getEnchantments(stack);
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag tag = enchantments.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
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

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Warden) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getSource().getDirectEntity() instanceof EndCrystal)) {
            return;
        }
        ItemEntity drop = new ItemEntity(level, entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                new ItemStack(ModItems.DEEP_CRYSTAL.get()));
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }
}
