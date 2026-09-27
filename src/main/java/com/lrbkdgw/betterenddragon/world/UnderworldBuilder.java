package com.lrbkdgw.betterenddragon.world;

import com.lrbkdgw.betterenddragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the single 500 block road of the Underworld, its railings, lanterns,
 * reward shulker boxes and the three special beacon towers.
 */
public final class UnderworldBuilder {
    private static final int ROAD_HALF_WIDTH = 2;
    private static final BlockState ROAD = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState EDGE = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState BASE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState RAILING = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
    private static final BlockState LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState PLATFORM = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState TOWER = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState TOWER_TOP = Blocks.GLOWSTONE.defaultBlockState();

    private UnderworldBuilder() {
    }

    public static void ensureBuilt(ServerLevel level) {
        UnderworldState state = UnderworldState.get(level);
        if (state.built) {
            return;
        }
        build(level);
        state.built = true;
        state.setDirty();
    }

    private static void build(ServerLevel level) {
        int y = ModDimensions.ROAD_Y;

        for (int x = -6; x <= ModDimensions.ROAD_LENGTH + 6; x++) {
            for (int z = -ROAD_HALF_WIDTH; z <= ROAD_HALF_WIDTH; z++) {
                set(level, x, y - 1, z, BASE);
                set(level, x, y, z, ROAD);
                clear(level, x, y + 1, z);
                clear(level, x, y + 2, z);
                clear(level, x, y + 3, z);
            }
            for (int side = -1; side <= 1; side += 2) {
                int z = side * (ROAD_HALF_WIDTH + 1);
                set(level, x, y - 1, z, BASE);
                set(level, x, y, z, EDGE);
                set(level, x, y + 1, z, RAILING);
                if (Math.floorMod(x, 5) == 0) {
                    set(level, x, y + 2, z, LANTERN);
                } else {
                    clear(level, x, y + 2, z);
                }
            }
        }

        int stations = ModDimensions.ROAD_LENGTH / ModDimensions.STATION_SPACING;
        for (int i = 1; i <= stations; i++) {
            buildStation(level, i, i * ModDimensions.STATION_SPACING, y);
        }

        // little arrival platform behind the spawn point
        for (int x = -6; x <= -3; x++) {
            for (int z = -3; z <= 3; z++) {
                set(level, x, y, z, PLATFORM);
            }
        }
    }

    private static void buildStation(ServerLevel level, int index, int x, int y) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                set(level, x + dx, y - 1, dz, BASE);
                set(level, x + dx, y, dz, PLATFORM);
                clear(level, x + dx, y + 1, dz);
                clear(level, x + dx, y + 2, dz);
            }
        }
        for (int dx = -4; dx <= 4; dx += 8) {
            for (int dz = -7; dz <= 7; dz += 14) {
                set(level, x + dx, y + 1, dz, RAILING);
                set(level, x + dx, y + 2, dz, LANTERN);
            }
        }

        if (index <= 7) {
            placeRewardBoxes(level, x, y, UnderworldRewards.station(index));
        } else {
            BlockState device = switch (index) {
                case 8 -> ModBlocks.KEEP_INVENTORY_DEVICE.get().defaultBlockState();
                case 9 -> ModBlocks.CREATIVE_FLIGHT_DEVICE.get().defaultBlockState();
                default -> ModBlocks.RETURN_DEVICE.get().defaultBlockState();
            };
            buildTower(level, x, y, device);
        }
    }

    private static void placeRewardBoxes(ServerLevel level, int x, int y, List<ItemStack> items) {
        List<BlockPos> spots = new ArrayList<>();
        for (int dz : new int[]{5, -5}) {
            for (int dx = -1; dx <= 1; dx++) {
                spots.add(new BlockPos(x + dx, y + 1, dz));
            }
        }

        int index = 0;
        int spot = 0;
        while (index < items.size() && spot < spots.size()) {
            BlockPos pos = spots.get(spot++);
            level.setBlock(pos, Blocks.SHULKER_BOX.defaultBlockState()
                    .setValue(ShulkerBoxBlock.FACING, Direction.UP), 2);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerBoxBlockEntity box) {
                for (int slot = 0; slot < box.getContainerSize() && index < items.size(); slot++) {
                    box.setItem(slot, items.get(index++));
                }
                box.setChanged();
            } else {
                index += 27;
            }
        }
    }

    private static void buildTower(ServerLevel level, int x, int y, BlockState device) {
        int centerZ = 5;
        for (int dy = 0; dy <= 12; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    set(level, x + dx, y + dy, centerZ + dz, dy == 12 ? TOWER_TOP : TOWER);
                }
            }
        }
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        for (int dy = 1; dy <= 14; dy++) {
            set(level, x, y + dy, centerZ - 2, ladder);
        }
        set(level, x, y + 13, centerZ, device);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    private static void clear(ServerLevel level, int x, int y, int z) {
        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
    }
}
