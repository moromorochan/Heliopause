package com.moromoro.heliopause.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class RectangularBlock extends Block {
    
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    
    public RectangularBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(UP,false)
            .setValue(DOWN, false)
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(EAST, false)
            .setValue(WEST, false)
        );
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
        BlockState newState = state;
        // 上下を確認
        newState = newState.setValue(UP, checkConnectivity(level, pos, Direction.UP));
        newState = newState.setValue(DOWN, checkConnectivity(level, pos, Direction.DOWN));
        // 側面の接続可能性チェック
        for (Direction direction : HORIZONTAL_FACING.getPossibleValues()){
            boolean connectivity = true;
            for (int relativeY = 0; relativeY < 512; relativeY++) {
                BlockPos localBlockPos = pos.above(relativeY);
                BlockPos localSidePos = localBlockPos.relative(direction);
                boolean toSide = checkConnectivity(level, localBlockPos, direction);
                boolean fromSide = checkConnectivity(level, localSidePos, direction.getOpposite());
                // 上端が同じかチェック
                if(!toSide && !fromSide){
                    break;
                }
                if(toSide && fromSide){
                    continue;
                }
                connectivity = false;
                break;
            }
            if(connectivity) {
                for (int relativeY = 0; relativeY < 512; relativeY++) {
                    BlockPos localBlockPos = pos.below(relativeY);
                    BlockPos localSidePos = localBlockPos.relative(direction);
                    boolean toSide = checkConnectivity(level, localBlockPos, direction);
                    boolean fromSide = checkConnectivity(level, localSidePos, direction.getOpposite());
                    // 下端が同じかチェック
                    if(!toSide && !fromSide){
                        break;
                    }
                    if(toSide && fromSide){
                        continue;
                    }
                    connectivity = false;
                    break;
                }
            }
            
            switch (direction){
                case NORTH -> newState = newState.setValue(NORTH, connectivity);
                case SOUTH -> newState = newState.setValue(SOUTH, connectivity);
                case WEST -> newState = newState.setValue(WEST, connectivity);
                case EAST -> newState = newState.setValue(EAST, connectivity);
            }
        }
        level.setBlock(pos, newState, 3);
        
    }
    
    private boolean checkConnectivity(Level level, BlockPos blockPos, Direction direction){
        return level.getBlockState(blockPos.relative(direction)).is(this.asBlock());
    }
}
