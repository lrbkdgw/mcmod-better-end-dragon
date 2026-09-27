package com.lrbkdgw.betterenddragon.dragon;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lrbkdgw.betterenddragon.crystal.CrystalType;
import com.lrbkdgw.betterenddragon.crystal.SpecialEndCrystal;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Watches the exit-portal frame of the End for the "all four special crystals" respawn ritual.
 *
 * <p>The vanilla respawn animation removes the crystals in the very same tick that the dragon is
 * created, so instead of trying to look at them afterwards we keep remembering the last time all
 * four variants were sitting on the frame together.</p>
 */
public final class RitualTracker {

    /** How long (in ticks) the memory stays valid - the vanilla respawn animation takes ~600 ticks. */
    private static final long MEMORY_TICKS = 2400L;

    private static final Map<ResourceKey<Level>, Long> LAST_FULL_RITUAL = new HashMap<>();

    private RitualTracker() {
    }

    public static void scan(ServerLevel level) {
        if (level.getDragonFight() == null) {
            return;
        }
        AABB box = new AABB(-8.0D, level.getMinBuildHeight(), -8.0D,
                9.0D, level.getMaxBuildHeight(), 9.0D);
        Set<CrystalType> found = EnumSet.noneOf(CrystalType.class);
        for (SpecialEndCrystal crystal : level.getEntitiesOfClass(SpecialEndCrystal.class, box)) {
            found.add(crystal.getCrystalType());
        }
        if (found.size() == CrystalType.values().length) {
            LAST_FULL_RITUAL.put(level.dimension(), level.getGameTime());
        }
    }

    /** @return {@code true} when the dragon that is spawning right now was summoned by the full ritual. */
    public static boolean consume(ServerLevel level) {
        Long recorded = LAST_FULL_RITUAL.get(level.dimension());
        if (recorded == null) {
            return false;
        }
        LAST_FULL_RITUAL.remove(level.dimension());
        return level.getGameTime() - recorded <= MEMORY_TICKS;
    }

    public static void clear() {
        LAST_FULL_RITUAL.clear();
    }
}
