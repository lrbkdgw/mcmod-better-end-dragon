package com.lrbkdgw.betterenddragon.crystal;

import java.util.List;

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

/**
 * Behaves exactly like the vanilla {@code EndCrystalItem}, but places a
 * {@link SpecialEndCrystal} carrying the correct {@link CrystalType}.
 */
public class SpecialEndCrystalItem extends Item {

    private final CrystalType type;

    public SpecialEndCrystalItem(CrystalType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public CrystalType getCrystalType() {
        return type;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return type.glint() || super.isFoil(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState state = level.getBlockState(clicked);
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        }

        BlockPos above = clicked.above();
        if (!level.isEmptyBlock(above)) {
            return InteractionResult.FAIL;
        }

        double x = above.getX();
        double y = above.getY();
        double z = above.getZ();
        List<Entity> blocking = level.getEntities((Entity) null, new AABB(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D));
        if (!blocking.isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            SpecialEndCrystal crystal = ModEntities.SPECIAL_END_CRYSTAL.get().create(level);
            if (crystal == null) {
                return InteractionResult.FAIL;
            }
            crystal.setCrystalType(this.type);
            crystal.moveTo(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
            crystal.setShowBottom(false);
            level.addFreshEntity(crystal);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, above);

            EndDragonFight fight = serverLevel.getDragonFight();
            if (fight != null) {
                fight.tryRespawn();
            }
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
