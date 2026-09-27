package com.lrbkdgw.betterenddragon.netherworld;

import java.util.List;

import com.lrbkdgw.betterenddragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds the single 500 block long road of the Netherworld.
 *
 * <p>The road runs along +X starting at the origin. Every 50 blocks there is a station: seven of
 * them hold fixed-content shulker boxes, the last three are the special beacon towers.</p>
 */
public final class NetherworldGenerator {

    public static final int FLOOR_Y = 64;
    public static final int ROAD_LENGTH = 500;
    public static final int STATION_SPACING = 50;

    public static final double SPAWN_X = 0.5D;
    public static final double SPAWN_Y = FLOOR_Y + 1;
    public static final double SPAWN_Z = 0.5D;

    private static final BlockState FLOOR = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState PILLAR = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState RAILING = Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
    private static final BlockState LAMP = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private NetherworldGenerator() {
    }

    public static void ensureGenerated(ServerLevel level) {
        NetherworldData data = NetherworldData.get(level);
        if (data.isGenerated()) {
            return;
        }
        data.setGenerated(true);
        build(level);
    }

    private static void build(ServerLevel level) {
        buildRoad(level);
        for (int station = 0; station < NetherworldRewards.STATION_COUNT; station++) {
            int x = (station + 1) * STATION_SPACING;
            buildStationPlatform(level, x);
            if (station <= 6) {
                buildRewardStation(level, x, station);
            } else {
                buildTower(level, x, station);
            }
        }
    }

    private static void buildRoad(ServerLevel level) {
        for (int x = -6; x <= ROAD_LENGTH + 8; x++) {
            for (int z = -3; z <= 3; z++) {
                set(level, x, FLOOR_Y, z, FLOOR);
            }
            if (nearStation(x)) {
                continue;
            }
            if (Math.floorMod(x, 5) == 0) {
                for (int z : new int[]{-3, 3}) {
                    set(level, x, FLOOR_Y + 1, z, PILLAR);
                    set(level, x, FLOOR_Y + 2, z, PILLAR);
                    set(level, x, FLOOR_Y + 3, z, LAMP);
                }
            } else {
                for (int z : new int[]{-3, 3}) {
                    set(level, x, FLOOR_Y + 1, z, RAILING);
                }
            }
        }
    }

    private static boolean nearStation(int x) {
        for (int station = 0; station < NetherworldRewards.STATION_COUNT; station++) {
            int sx = (station + 1) * STATION_SPACING;
            if (Math.abs(x - sx) <= 6) {
                return true;
            }
        }
        return false;
    }

    private static void buildStationPlatform(ServerLevel level, int x0) {
        for (int x = x0 - 6; x <= x0 + 6; x++) {
            for (int z = -7; z <= 7; z++) {
                set(level, x, FLOOR_Y, z, FLOOR);
                for (int y = FLOOR_Y + 1; y <= FLOOR_Y + 4; y++) {
                    set(level, x, y, z, AIR);
                }
            }
        }
        // railings around the platform, leaving the road itself open on both ends
        for (int x = x0 - 6; x <= x0 + 6; x++) {
            set(level, x, FLOOR_Y + 1, -7, RAILING);
            set(level, x, FLOOR_Y + 1, 7, RAILING);
        }
        for (int z = -7; z <= 7; z++) {
            if (Math.abs(z) <= 3) {
                continue; // the road runs through here
            }
            set(level, x0 - 6, FLOOR_Y + 1, z, RAILING);
            set(level, x0 + 6, FLOOR_Y + 1, z, RAILING);
        }
        // corner lights
        for (int dx : new int[]{-6, 6}) {
            for (int dz : new int[]{-7, 7}) {
                set(level, x0 + dx, FLOOR_Y + 1, dz, PILLAR);
                set(level, x0 + dx, FLOOR_Y + 2, dz, PILLAR);
                set(level, x0 + dx, FLOOR_Y + 3, dz, LAMP);
            }
        }
    }

    private static void buildRewardStation(ServerLevel level, int x0, int station) {
        List<List<ItemStack>> boxes = NetherworldRewards.intoBoxes(NetherworldRewards.contents(station));
        int count = Math.max(1, boxes.size());
        for (int i = 0; i < boxes.size(); i++) {
            int x = x0 - (count - 1) + i * 2;
            BlockPos pos = new BlockPos(x, FLOOR_Y + 1, 5);
            set(level, x, FLOOR_Y + 1, 5, Blocks.SHULKER_BOX.defaultBlockState());
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
                List<ItemStack> contents = boxes.get(i);
                for (int slot = 0; slot < contents.size() && slot < shulker.getContainerSize(); slot++) {
                    shulker.setItem(slot, contents.get(slot));
                }
                shulker.setChanged();
            }
        }
    }

    private static void buildTower(ServerLevel level, int x0, int station) {
        int top = FLOOR_Y + 11;
        // shaft
        for (int y = FLOOR_Y + 1; y < top; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    set(level, x0 + dx, y, dz, PILLAR);
                }
            }
        }
        // top platform (with a hole for the ladder). Iron blocks so the beacon actually has a base.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 2) {
                    continue;
                }
                set(level, x0 + dx, top, dz, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
        // ladder - it has to reach one block above the platform so that the player can step off it
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        set(level, x0, top + 1, 1, PILLAR); // the ladder's top anchor
        for (int y = FLOOR_Y + 1; y <= top + 1; y++) {
            set(level, x0, y, 2, ladder);
        }
        // lantern crown
        for (int dx : new int[]{-2, 2}) {
            for (int dz : new int[]{-2, 2}) {
                set(level, x0 + dx, top + 1, dz, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        set(level, x0, top + 1, -1, Blocks.BEACON.defaultBlockState());

        BlockState device = switch (station) {
            case 7 -> ModBlocks.KEEP_INVENTORY_DEVICE.get().defaultBlockState();
            case 8 -> ModBlocks.CREATIVE_FLIGHT_DEVICE.get().defaultBlockState();
            default -> ModBlocks.RETURN_HOME_DEVICE.get().defaultBlockState();
        };
        set(level, x0, top + 1, 0, device);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }
}
