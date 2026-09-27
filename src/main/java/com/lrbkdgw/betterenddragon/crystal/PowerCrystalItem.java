package com.lrbkdgw.betterenddragon.crystal;

import com.lrbkdgw.betterenddragon.dragon.DragonRitual;
import com.lrbkdgw.betterenddragon.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Behaves exactly like the vanilla end crystal item but places the matching
 * {@link PowerCrystalEntity} and arms the enhanced dragon ritual.
 */
public class PowerCrystalItem extends Item {
    private final CrystalKind kind;

    public PowerCrystalItem(Properties properties, CrystalKind kind) {
        super(properties);
        this.kind = kind;
    }

    public CrystalKind getKind() {
        return this.kind;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState state = level.getBlockState(clicked);
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        }

        BlockPos target = clicked.above();
        if (!level.isEmptyBlock(target)) {
            return InteractionResult.FAIL;
        }

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
        List<Entity> blocking = level.getEntities((Entity) null, new AABB(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D));
        if (!blocking.isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            PowerCrystalEntity crystal = ModEntities.byKind(this.kind).get().create(serverLevel);
            if (crystal == null) {
                return InteractionResult.FAIL;
            }
            crystal.setPos(x + 0.5D, y, z + 0.5D);
            crystal.setShowBottom(false);
            serverLevel.addFreshEntity(crystal);
            serverLevel.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, target);

            EndDragonFight fight = serverLevel.getDragonFight();
            if (fight != null) {
                DragonRitual.checkAndArm(serverLevel, target);
                fight.tryRespawn();
            }
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
