package com.lrbkdgw.betterenddragon.world;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contents of the Underworld reward shulker boxes.
 */
public final class UnderworldRewards {
    public static final Item[] SMITHING_TEMPLATES = new Item[]{
            Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
            Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
            Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE
    };

    private UnderworldRewards() {
    }

    /**
     * @param station 1 based station index.
     * @return the items of that station, empty when the station is a beacon tower.
     */
    public static List<ItemStack> station(int station) {
        List<ItemStack> items = new ArrayList<>();
        switch (station) {
            case 1 -> {
                add(items, Items.ELYTRA, 8);
                add(items, Items.NETHERITE_BLOCK, 16);
                add(items, Items.DIAMOND_BLOCK, 128);
                add(items, Items.EMERALD_BLOCK, 128);
                add(items, Items.REDSTONE_BLOCK, 256);
                add(items, Items.LAPIS_BLOCK, 256);
                add(items, Items.IRON_BLOCK, 256);
                add(items, Items.GOLD_BLOCK, 256);
                add(items, Items.COPPER_BLOCK, 256);
            }
            case 2 -> {
                add(items, Items.ENCHANTED_GOLDEN_APPLE, 256);
                for (Item template : SMITHING_TEMPLATES) {
                    add(items, template, 32);
                }
                add(items, Items.AMETHYST_SHARD, 256);
                add(items, Items.NETHER_STAR, 8);
                add(items, Items.SHULKER_SHELL, 128);
            }
            case 3 -> {
                for (int i = 0; i < 9; i++) {
                    items.add(potion(MobEffects.DAMAGE_RESISTANCE, 4, 72000,
                            "item.betterenddragon.potion.resistance"));
                }
                for (int i = 0; i < 9; i++) {
                    items.add(potion(MobEffects.REGENERATION, 9, 144000,
                            "item.betterenddragon.potion.regeneration"));
                }
                for (int i = 0; i < 9; i++) {
                    items.add(potion(MobEffects.DAMAGE_BOOST, 254, 400,
                            "item.betterenddragon.potion.strength"));
                }
            }
            case 4 -> {
                items.add(unbreakable(new ItemStack(Items.DIAMOND_HELMET)));
                items.add(unbreakable(new ItemStack(Items.DIAMOND_CHESTPLATE)));
                items.add(unbreakable(new ItemStack(Items.DIAMOND_LEGGINGS)));
                items.add(unbreakable(new ItemStack(Items.DIAMOND_BOOTS)));
            }
            case 5 -> {
                items.add(unbreakable(new ItemStack(Items.ELYTRA)));
                for (int i = 0; i < 18; i++) {
                    items.add(firework(64));
                }
            }
            case 6 -> {
                items.add(tool(Items.DIAMOND_PICKAXE));
                items.add(tool(Items.DIAMOND_HOE));
                items.add(tool(Items.DIAMOND_AXE));
            }
            case 7 -> {
                ItemStack sword = unbreakable(new ItemStack(Items.DIAMOND_SWORD));
                sword.enchant(Enchantments.SHARPNESS, 10);
                sword.enchant(Enchantments.MOB_LOOTING, 10);
                sword.enchant(Enchantments.FIRE_ASPECT, 10);
                items.add(sword);
            }
            default -> {
                return Collections.emptyList();
            }
        }
        return items;
    }

    private static ItemStack tool(Item item) {
        ItemStack stack = unbreakable(new ItemStack(item));
        stack.enchant(Enchantments.BLOCK_FORTUNE, 10);
        stack.enchant(Enchantments.BLOCK_EFFICIENCY, 10);
        return stack;
    }

    private static ItemStack firework(int count) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, count);
        stack.getOrCreateTagElement("Fireworks").putByte("Flight", (byte) 3);
        return stack;
    }

    public static ItemStack unbreakable(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        return stack;
    }

    private static ItemStack potion(MobEffect effect, int amplifier, int duration, String nameKey) {
        ItemStack stack = new ItemStack(Items.POTION);
        PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(effect, duration, amplifier)));
        stack.getOrCreateTag().putInt("CustomPotionColor", effect.getColor());
        stack.setHoverName(Component.translatable(nameKey));
        return stack;
    }

    private static void add(List<ItemStack> items, Item item, int total) {
        int maxStack = new ItemStack(item).getMaxStackSize();
        int left = total;
        while (left > 0) {
            int size = Math.min(maxStack, left);
            items.add(new ItemStack(item, size));
            left -= size;
        }
    }

    /** Helper for enchanting with levels above the vanilla maximum. */
    public static void enchant(ItemStack stack, Enchantment enchantment, int level) {
        stack.enchant(enchantment, level);
    }
}
