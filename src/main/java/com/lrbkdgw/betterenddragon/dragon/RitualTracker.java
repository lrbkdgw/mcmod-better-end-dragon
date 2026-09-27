package com.lrbkdgw.betterenddragon.dragon;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lrbkdgw.betterenddragon.crystal.CrystalType;
import com.lrbkdgw.betterenddragon.crystal.SpecialEndCrystal;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Watches the exit-portal frame of the End for the "all four special crystals" respawn ritual.
 *
 * <p>The vanilla respawn animation removes the ritual crystals long before the dragon entity is
 * actually created, so instead of trying to look at them afterwards we remember the last time the
 * four portal pedestals held one of each variant.</p>
 *
 * <p>Vanilla looks for crystals sitting exactly two blocks away from the portal centre in each
 * horizontal direction ({@code EndDragonFight#tryRespawn}), so we use the very same geometry
 * instead of a loose proximity check - four wither crystals plus a few loose ones lying around
 * must <em>not</em> count as the full ritual.</p>
 */
public final class RitualTracker {

    /** How long (in ticks) the memory stays valid - the vanilla respawn animation takes ~600 ticks. */
    private static final long MEMORY_TICKS = 2400L;

    /** How far away from the world origin the exit portal is looked for. */
    private static final double SEARCH_RADIUS = 24.0D;

    private static final Map<ResourceKey<Level>, Long> LAST_FULL_RITUAL = new HashMap<>();

    private RitualTracker() {
    }

    public static void scan(ServerLevel level) {
        if (level.getDragonFight() == null) {
            return;
        }
        AABB box = new AABB(-SEARCH_RADIUS, level.getMinBuildHeight(), -SEARCH_RADIUS,
                SEARCH_RADIUS, level.getMaxBuildHeight(), SEARCH_RADIUS);
        List<SpecialEndCrystal> crystals = level.getEntitiesOfClass(SpecialEndCrystal.class, box);
        if (crystals.size() < CrystalType.values().length) {
            return;
        }

        // Every crystal could be any of the four pedestals, so try each implied portal centre.
        List<long[]> centres = new ArrayList<>();
        for (SpecialEndCrystal crystal : crystals) {
            int x = Mth.floor(crystal.getX());
            int z = Mth.floor(crystal.getZ());
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                centres.add(new long[]{x - direction.getStepX() * 2L, z - direction.getStepZ() * 2L});
            }
        }

        for (long[] centre : centres) {
            if (isFullRitual(crystals, centre[0], centre[1])) {
                LAST_FULL_RITUAL.put(level.dimension(), level.getGameTime());
                return;
            }
        }
    }

    private static boolean isFullRitual(List<SpecialEndCrystal> crystals, long centreX, long centreZ) {
        Set<CrystalType> found = EnumSet.noneOf(CrystalType.class);
        int pedestals = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            long px = centreX + direction.getStepX() * 2L;
            long pz = centreZ + direction.getStepZ() * 2L;
            SpecialEndCrystal seated = null;
            for (SpecialEndCrystal crystal : crystals) {
                if (Mth.floor(crystal.getX()) == px && Mth.floor(crystal.getZ()) == pz) {
                    seated = crystal;
                    break;
                }
            }
            if (seated == null) {
                return false;
            }
            pedestals++;
            found.add(seated.getCrystalType());
        }
        return pedestals == 4 && found.size() == CrystalType.values().length;
    }

    /** @return {@code true} when the dragon that is spawning right now was summoned by the full ritual. */
    public static boolean consume(ServerLevel level) {
        Long recorded = LAST_FULL_RITUAL.remove(level.dimension());
        return recorded != null && level.getGameTime() - recorded <= MEMORY_TICKS;
    }

    public static void clear() {
        LAST_FULL_RITUAL.clear();
    }
}
