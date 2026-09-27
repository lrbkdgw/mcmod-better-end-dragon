package com.example.betterenddragon;

import net.minecraft.world.item.Item;

/** Ritual crystals are intentionally ordinary items: they can be moved, stored and used in recipes. */
public class CrystalItem extends Item {
    public CrystalItem(Properties properties) { super(properties.stacksTo(64)); }
}
