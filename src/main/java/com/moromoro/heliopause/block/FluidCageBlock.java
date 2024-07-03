package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.FluidCageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidCageBlock extends AbstractFluidTankBlock{

    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public BlockPos pairBlockPos;
    private Level level;

    public FluidCageBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public static VoxelShape LOWER_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    public static VoxelShape UPPER_SHAPE = Block.box(2,0,2,14,15.995,14);

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(blockState.getValue(HALF) == DoubleBlockHalf.UPPER){
            return UPPER_SHAPE;
        }else{
            return LOWER_SHAPE;
        }
    }

    //設置する直前の挙動
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        //設置時の向きを取得
        Direction direction = context.getHorizontalDirection().getOpposite();
        //位置をふたつ取得
        BlockPos lowerPos = context.getClickedPos();//.relative(context.getClickedFace());
        BlockPos upperPos = lowerPos.above();
        //上側にもブロックが設置できるなら
        if (context.getLevel().getBlockState(upperPos).canBeReplaced(context)) {
            //インタラクト位置のブロック状態を設定
            return this.defaultBlockState().setValue(DIRECTION, direction).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        //上側に置けない場合、下も設置しない
        return null;
    }

    //設置した直後の挙動
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isNatural) {
        super.onPlace(state, level, pos, oldState, isNatural);
        //設置されたのが下側なら
        if(state.getValue(HALF)==DoubleBlockHalf.LOWER){
            //上にブロックを設置
            BlockPos upperPos = pos.above();
            BlockState upperState = this.defaultBlockState().setValue(DIRECTION, state.getValue(DIRECTION)).setValue(HALF, DoubleBlockHalf.UPPER);
            level.setBlock(upperPos, upperState,0b111);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION, HALF);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FluidCageBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        if(blockState.getBlock() != newBlockState.getBlock()){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof FluidCageBlockEntity){
                ((FluidCageBlockEntity)blockEntity).setRemoved();
            }
            //下のブロックが破壊されたとき、上のブロックも破壊する
            if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER && level.getBlockState(pos.above()).getBlock() == this) {
                level.destroyBlock(pos.above(), false);
            }
            //上のブロックが破壊されたとき、下のブロックも破壊する
            if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER && level.getBlockState(pos.below()).getBlock() == this) {
                level.destroyBlock(pos.below(), false);
            }
        }
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }
}
