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
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidCageBlock extends AbstractFluidTankBlock{

    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FLOOR = BlockStateProperties.VERTICAL_DIRECTION;

    public FluidCageBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DIRECTION, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FLOOR, Direction.UP));
    }

    public static VoxelShape BASE_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    public static VoxelShape LOWER_CULL = Block.box(2, 0.005, 2, 14, 16, 14);
    public static VoxelShape UPPER_CULL = Block.box(2,0,2,14,15.995,14);

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(blockState.getValue(HALF) == DoubleBlockHalf.UPPER){
            if(blockState.getValue(FLOOR) == Direction.UP){
                return UPPER_CULL;
            }else{
                return LOWER_CULL;
            }
        }else{
            return BASE_SHAPE;
        }
    }

    //設置する直前の挙動
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        //設置時の向きを取得
        Direction direction = context.getHorizontalDirection().getOpposite();
        boolean placeOnCeil = context.getClickedFace() == Direction.DOWN;
        //位置をふたつ取得
        BlockPos lowerPos = placeOnCeil ? context.getClickedPos().below() : context.getClickedPos();
        BlockPos upperPos = lowerPos.above();

        //ペアのブロックを設置できるか確認
        if(
                context.getLevel().getBlockState(upperPos).canBeReplaced(context) &&
                context.getLevel().getBlockState(lowerPos).canBeReplaced(context)
        ){
            return this.defaultBlockState()
                    .setValue(DIRECTION, direction)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(FLOOR, placeOnCeil? Direction.DOWN : Direction.UP);
        }
        /*
        //設置がbottomのとき、上側のブロックが設置できるなら
        if (placeOnCeil && context.getLevel().getBlockState(upperPos).canBeReplaced(context)) {
            //インタラクト位置のブロック状態を設定
            return this.defaultBlockState()
                    .setValue(DIRECTION, direction)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(FLOOR, Direction.DOWN);
        }
        //設置がtopのとき、下側のブロックが設置できるなら
        else if(!placeOnCeil && context.getLevel().getBlockState(lowerPos).canBeReplaced(context)){
            //インタラクト位置のブロック状態を設定
            return this.defaultBlockState()
                    .setValue(DIRECTION, direction)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(FLOOR, Direction.UP);
        }*/
        //片方に置けない場合、もう片方も設置しない
        return null;
    }

    //設置した直後の挙動
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isNatural) {
        super.onPlace(state, level, pos, oldState, isNatural);
        //設置されたのが弓側のブロックなら挙動を終了
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) {return;}

        //弓側のブロック位置を取得
        boolean placeOnCeil = state.getValue(FLOOR) == Direction.DOWN;
        BlockPos pairBlockPos = placeOnCeil ? pos.below():pos.above();
        //弓側のブロックステートを用意
        BlockState pairBlockState = this.defaultBlockState()
                .setValue(DIRECTION, state.getValue(DIRECTION))
                .setValue(HALF,DoubleBlockHalf.UPPER)
                .setValue(FLOOR,state.getValue(FLOOR));

        //弓側のブロックを設置
        level.setBlock(pairBlockPos, pairBlockState,0b111);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION, HALF, FLOOR);
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
            BlockPos pairBlockPos;
            if(blockEntity instanceof FluidCageBlockEntity){
                //ペア破壊
                pairBlockPos = ((FluidCageBlockEntity)blockEntity).getPairBlockPos();
                if(pairBlockPos!=null){
                    level.destroyBlock(pairBlockPos,false);
                }
                //自身のブロックエンティティを削除
                ((FluidCageBlockEntity)blockEntity).setRemoved();
            }
        }
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }
}
