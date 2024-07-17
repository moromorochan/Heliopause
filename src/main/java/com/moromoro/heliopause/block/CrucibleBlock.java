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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlock extends AbstractFluidTankBlock {

    public CrucibleBlock(Properties p_49224_) {
        super(p_49224_);
    }

    public static VoxelShape SHAPE =
            Shapes.join(
                    Shapes.join(
                            Block.box(0, 4, 0, 16, 16, 16),
                            Block.box(2, 5, 2, 14, 16, 14),
                            BooleanOp.ONLY_FIRST
                    ),
                    Block.box(2,2,2,14,4,14),
                    BooleanOp.OR
            );

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

}
