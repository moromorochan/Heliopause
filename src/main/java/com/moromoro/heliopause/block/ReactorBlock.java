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

public class ReactorBlock extends HorizontalFacingBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ReactorBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F)
                .sound(SoundType.LANTERN)
                .lightLevel(state -> state.getValue(LIT) ? 15 : 9)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(DIRECTION, Direction.NORTH));
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
        return Shapes.join(
                Block.box(0,0,0,16,3,16),
                Block.box(1,3,1,15,13,15),
                BooleanOp.OR
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return null;
    }
}
