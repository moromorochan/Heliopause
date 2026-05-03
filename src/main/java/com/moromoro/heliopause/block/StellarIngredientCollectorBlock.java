package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.StellarIngredientCollectorBlockEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StellarIngredientCollectorBlock extends DirectionalEntityBlock {
    private final static double WIDTH = 10.0 / 16.0;
    private final static double HEIGHT = 7.0 / 16.0;
    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public StellarIngredientCollectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP).setValue(POWERED, false));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
    
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter getter, BlockPos blockPos, CollisionContext context) {
        double widthMin = 0.5 - WIDTH/2;
        double widthMax = 0.5 + WIDTH/2;
        
        return switch (blockState.getValue(FACING)){
            case DOWN -> Shapes.box(widthMin, 1 - HEIGHT, widthMin, widthMax, 1, widthMax);
            case UP -> Shapes.box(widthMin, 0, widthMin, widthMax, HEIGHT,widthMax);
            case NORTH -> Shapes.box(widthMin, widthMin, 1 - HEIGHT, widthMax, widthMax,1);
            case SOUTH -> Shapes.box(widthMin, widthMin, 0, widthMax, widthMax,HEIGHT);
            case WEST -> Shapes.box(1 - HEIGHT, widthMin, widthMin, 1, widthMax,widthMax);
            case EAST -> Shapes.box(0, widthMin, widthMin, HEIGHT, widthMax,widthMax);
        };
    }
    
    // レッドストーン用
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
        
        // 接続先以外ならレッドストーン判定
        boolean hasSignal = getNeighborSignal(level, pos, state.getValue(FACING).getOpposite());
        if (hasSignal != state.getValue(POWERED)) {
            // 状態を更新
            level.setBlock(pos, state.setValue(POWERED, hasSignal), 3);
        }
    }
    
    private boolean getNeighborSignal(Level level, BlockPos pos, Direction denyDirection){
        for (Direction direction : Direction.values()) {
            if(direction.equals(denyDirection)){
                continue;
            }
            if(level.getSignal(pos.relative(direction), direction) > 0){
                return true;
            }
        }
        return false;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new StellarIngredientCollectorBlockEntity(blockPos, blockState);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.INGREDIENT_COLLECTOR_BE.get(),
            (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState, blockEntity)
        );
    }
}
