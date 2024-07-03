package com.moromoro.heliopause.block;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.NotNull;

public class AlchemyStoveBlock extends HorizontalFacingEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AlchemyStoveBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F)
                .sound(SoundType.LANTERN)
                .lightLevel(blockState -> blockState.getValue(LIT) ? 15 : 9)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(DIRECTION, Direction.NORTH));
    }

    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context){
        return Shapes.join(Shapes.join(
                        Block.box(0,0,0,16,3,16),
                        Block.box(1,3,1,15,9,15),
                        BooleanOp.OR
                ),
                Block.box(0,9,0,16,13,16),
                BooleanOp.OR
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return null;
    }
}
