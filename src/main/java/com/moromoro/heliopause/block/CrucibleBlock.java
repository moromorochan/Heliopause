package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlock extends AbstractFluidTankBlock {

    public CrucibleBlock(Properties p_49224_) {
        super(p_49224_);
    }

    public static VoxelShape SHAPE = Shapes.join(
            Block.box(0, 3, 0, 16, 16, 16),
            Block.box(2, 6, 2, 14, 16, 14),
            BooleanOp.ONLY_FIRST
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }
}
