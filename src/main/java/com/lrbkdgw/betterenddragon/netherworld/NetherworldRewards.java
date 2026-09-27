package com.lrbkdgw.betterenddragon.netherworld;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.world.level.ItemLike;

/**
 * The fixed (non loot-table) contents of the Netherworld reward shulker boxes.
 */
public final class NetherworldRewards {

    /** Number of stations along the road - one every 50 blocks. */
    public static final int STATION_COUNT = 10;

    private static final Item[] SMITHING_TEMPLATES = {
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
            Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
    };

    private NetherworldRewards() {
    }

    /** @return the items stored at the given station, or an empty list if the station is a beacon tower. */
    public static List<ItemStack> contents(int station) {
        List<ItemStack> out = new ArrayList<>();
        switch (station) {
            case 0 -> {
                add(out, Items.ELYTRA, 8);
                add(out, Items.NETHERITE_BLOCK, 16);
                add(out, Items.DIAMOND_BLOCK, 128);
                add(out, Items.EMERALD_BLOCK, 128);
                add(out, Items.REDSTONE_BLOCK, 256);
                add(out, Items.LAPIS_BLOCK, 256);
                add(out, Items.IRON_BLOCK, 256);
                add(out, Items.GOLD_BLOCK, 256);
                add(out, Items.COPPER_BLOCK, 256);
            }
            case 1 -> {
                add(out, Items.ENCHANTED_GOLDEN_APPLE, 256);
                for (Item template : SMITHING_TEMPLATES) {
                    add(out, template, 32);
                }
                add(out, Items.AMETHYST_SHARD, 256);
                add(out, Items.NETHER_STAR, 8);
                add(out, Items.SHULKER_SHELL, 128);
            }
            case 2 -> {
                for (int i = 0; i < 9; i++) {
                    out.add(potion(MobEffects.DAMAGE_RESISTANCE, 60 * 60 * 20, 4,
                            "item.betterenddragon.potion.resistance"));
                }
                for (int i = 0; i < 9; i++) {
                    out.add(potion(MobEffects.REGENERATION, 120 * 60 * 20, 9,
                            "item.betterenddragon.potion.regeneration"));
                }
                for (int i = 0; i < 9; i++) {
                    out.add(potion(MobEffects.DAMAGE_BOOST, 20 * 20, 254,
                            "item.betterenddragon.potion.strength"));
                }
            }
            case 3 -> {
                out.add(unbreakable(Items.DIAMOND_HELMET));
                out.add(unbreakable(Items.DIAMOND_CHESTPLATE));
                out.add(unbreakable(Items.DIAMOND_LEGGINGS));
                out.add(unbreakable(Items.DIAMOND_BOOTS));
            }
            case 4 -> {
                out.add(unbreakable(Items.ELYTRA));
                for (int i = 0; i < 18; i++) {
                    ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 64);
                    rocket.getOrCreateTagElement("Fireworks").putByte("Flight", (byte) 3);
                    out.add(rocket);
                }
            }
            case 5 -> {
                out.add(tool(Items.DIAMOND_PICKAXE));
                out.add(tool(Items.DIAMOND_HOE));
                out.add(tool(Items.DIAMOND_AXE));
            }
            case 6 -> {
                ItemStack sword = unbreakable(Items.DIAMOND_SWORD);
                enchant(sword, Enchantments.SHARPNESS, 10);
                enchant(sword, Enchantments.MOB_LOOTING, 10);
                enchant(sword, Enchantments.FIRE_ASPECT, 10);
                out.add(sword);
            }
            default -> {
                // stations 7..9 are the beacon towers
            }
        }
        return out;
    }

    /** Splits a reward into chunks of 27 - one shulker box each. */
    public static List<List<ItemStack>> intoBoxes(List<ItemStack> items) {
        List<List<ItemStack>> boxes = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 27) {
            boxes.add(new ArrayList<>(items.subList(i, Math.min(items.size(), i + 27))));
        }
        return boxes;
    }

    private static void add(List<ItemStack> out, ItemLike item, int count) {
        int max = new ItemStack(item).getMaxStackSize();
        int remaining = count;
        while (remaining > 0) {
            int size = Math.min(max, remaining);
            out.add(new ItemStack(item, size));
            remaining -= size;
        }
    }

    private static ItemStack unbreakable(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        return stack;
    }

    private static ItemStack tool(Item item) {
        ItemStack stack = unbreakable(item);
        enchant(stack, Enchantments.BLOCK_FORTUNE, 10);
        enchant(stack, Enchantments.BLOCK_EFFICIENCY, 10);
        return stack;
    }

    private static void enchant(ItemStack stack, Enchantment enchantment, int level) {
        stack.enchant(enchantment, level);
    }

    private static ItemStack potion(MobEffect effect, int duration, int amplifier, String nameKey) {
        ItemStack stack = new ItemStack(Items.POTION);
        PotionUtils.setCustomEffects(stack, List.of(new MobEffectInstance(effect, duration, amplifier)));
        stack.setHoverName(Component.translatable(nameKey));
        return stack;
    }
}
