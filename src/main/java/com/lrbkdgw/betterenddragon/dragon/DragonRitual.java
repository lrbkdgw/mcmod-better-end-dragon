package com.lrbkdgw.betterenddragon.dragon;

import com.lrbkdgw.betterenddragon.crystal.CrystalKind;
import com.lrbkdgw.betterenddragon.crystal.PowerCrystalEntity;
import com.lrbkdgw.betterenddragon.state.EndFightState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Detects the "one of each power crystal" summoning ritual on the exit portal.
 */
public final class DragonRitual {
    private DragonRitual() {
    }

    public static void checkAndArm(ServerLevel level, BlockPos placedCrystalPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos center = placedCrystalPos.relative(direction, 2);
            if (isFullRitual(level, center)) {
                EndFightState.get(level).arm();
                return;
            }
        }
    }

    private static boolean isFullRitual(ServerLevel level, BlockPos center) {
        EnumSet<CrystalKind> kinds = EnumSet.noneOf(CrystalKind.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class,
                    new AABB(center.relative(direction, 2)));
            if (crystals.size() != 1) {
                return false;
            }
            if (!(crystals.get(0) instanceof PowerCrystalEntity crystal)) {
                return false;
            }
            kinds.add(crystal.getKind());
        }
        return kinds.size() == CrystalKind.values().length;
    }
}
