package com.moromoro.heliopause.block;

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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class AbstractNetworkBlock extends Block {

    public static final IntegerProperty CENTER = IntegerProperty.create("center",0,3);
    public static final IntegerProperty NORTH = IntegerProperty.create("north",0,3);
    public static final IntegerProperty EAST = IntegerProperty.create("east",0,3);
    public static final IntegerProperty SOUTH = IntegerProperty.create("south",0,3);
    public static final IntegerProperty WEST = IntegerProperty.create("west",0,3);
    public static final IntegerProperty UP = IntegerProperty.create("up",0,3);
    public static final IntegerProperty DOWN = IntegerProperty.create("down",0,3);

    public AbstractNetworkBlock(Properties properties) {
        super(properties);
    }
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context){
        BlockState placementState = super.getStateForPlacement(context);
        Direction facing = context.getClickedFace();
        return placementState
                .setValue( NORTH, facing==Direction.NORTH || facing.getOpposite()==Direction.NORTH ? 1:0)
                .setValue( EAST, facing==Direction.EAST || facing.getOpposite()==Direction.EAST ? 1:0)
                .setValue( SOUTH, facing==Direction.SOUTH || facing.getOpposite()==Direction.SOUTH ? 1:0)
                .setValue( WEST, facing==Direction.WEST || facing.getOpposite()==Direction.WEST ? 1:0)
                .setValue( UP, facing==Direction.UP || facing.getOpposite()==Direction.UP ? 1:0)
                .setValue( DOWN, facing==Direction.DOWN || facing.getOpposite()==Direction.DOWN ? 1:0)
                .setValue(CENTER,1);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape CenterShape = Block.box(
                7-blockState.getValue(CENTER),7-blockState.getValue(CENTER),7-blockState.getValue(CENTER),
                9+blockState.getValue(CENTER),9+blockState.getValue(CENTER),9+blockState.getValue(CENTER)
        );
        VoxelShape NorthShape = Block.box(
                7-blockState.getValue(NORTH),7-blockState.getValue(NORTH),0,
                9+blockState.getValue(NORTH),9+blockState.getValue(NORTH),7
        );
        VoxelShape SouthShape = Block.box(
                7-blockState.getValue(SOUTH),7-blockState.getValue(SOUTH),9,
                9+blockState.getValue(SOUTH),9+blockState.getValue(SOUTH),16
        );
        VoxelShape EastShape = Block.box(
                9,7-blockState.getValue(EAST),7-blockState.getValue(EAST),
                16,9+blockState.getValue(EAST),9+blockState.getValue(EAST)
        );
        VoxelShape WestShape = Block.box(
                0,7-blockState.getValue(WEST),7-blockState.getValue(WEST),
                7,9+blockState.getValue(WEST),9+blockState.getValue(WEST)
        );

        VoxelShape UpShape = Block.box(
                7-blockState.getValue(UP),9,7-blockState.getValue(UP),
                9+blockState.getValue(UP),16,9+blockState.getValue(UP)
        );
        VoxelShape DownShape = Block.box(
                7-blockState.getValue(DOWN),0,7-blockState.getValue(DOWN),
                9+blockState.getValue(DOWN),7,9+blockState.getValue(DOWN)
        );

        VoxelShape CombinedShape = CenterShape;
        if (blockState.getValue(NORTH) > 0) CombinedShape = Shapes.or(CombinedShape, NorthShape);
        if (blockState.getValue(SOUTH) > 0) CombinedShape = Shapes.or(CombinedShape, SouthShape);
        if (blockState.getValue(EAST) > 0) CombinedShape = Shapes.or(CombinedShape, EastShape);
        if (blockState.getValue(WEST) > 0) CombinedShape = Shapes.or(CombinedShape, WestShape);
        if (blockState.getValue(UP) > 0) CombinedShape = Shapes.or(CombinedShape, UpShape);
        if (blockState.getValue(DOWN) > 0) CombinedShape = Shapes.or(CombinedShape, DownShape);

        return CombinedShape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add( NORTH, EAST, SOUTH, WEST, UP, DOWN, CENTER);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);

        //自身を更新
        state = connectState(state, level, pos);
        level.setBlock(pos, state, 3);

        //隣を更新
        BlockState neighborState = level.getBlockState(fromPos);
        if (neighborState.getBlock() instanceof AbstractNetworkBlock) {
            neighborState = connectState(neighborState, level, fromPos);
            level.setBlock(fromPos, neighborState, 3);
        }
    }

    public int getDiameter(BlockState blockState) {
        return blockState.getValue(CENTER);
    }

    private BlockState connectState(BlockState state, Level level, BlockPos pos) {
        //接続可能な方向を格納する配列
        HashMap<Direction, Integer> connectedDirections = new HashMap<Direction, Integer>();
        //配列の中身を用意
        for (Direction direction : Direction.values()) {
            //隣のブロックの情報を取得
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            Block neighborBlock = neighborState.getBlock();
            BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);

            //隣が同じ種類のブロックの場合
            if (neighborBlock instanceof AbstractNetworkBlock) {
                //方角側のパイプ太さを隣のブロックの太さにする
                int connectDiameter = Math.min(getDiameter(state),getDiameter(neighborState));
                connectedDirections.put(direction,connectDiameter);
            }
            //隣が液体を扱うブロックエンティティの場合
            else if (neighborBlockEntity != null) {
                LazyOptional<IFluidHandler> capability = neighborBlockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite());
                if (capability.isPresent()) {
                    //方角側のパイプ太さを自身の太さにする
                    connectedDirections.put(direction,getDiameter(state));
                }
            }

        }
        //ブロックステートを設定
        BlockState connectedState = state;
            //それぞれの方向に対して
        for (Direction direction : Direction.values()) {
            //接続数が0でないなら
            if (!connectedDirections.isEmpty()) {
                // 接続方向(接続数が1なら反対側も)を接続状態に
                if (connectedDirections.containsKey(direction))
                {
                    connectedState = connectedState.setValue(getProperty(direction),connectedDirections.get(direction));
                }
                else if(connectedDirections.size()==1 && connectedDirections.containsKey(direction.getOpposite())){
                    connectedState = connectedState.setValue(getProperty(direction),getDiameter(state));
                }
                //未接続方向を未接続状態に
                else {
                    connectedState = connectedState.setValue(getProperty(direction), 0);
                }
            }
            //接続数が0なら、もとある接続の太さをcenterに揃える
            else{
                connectedState = connectedState.setValue(getProperty(direction), state.getValue(getProperty(direction))>0 ? getDiameter(state):0);
            }
        }
        return connectedState;
    }

    private static IntegerProperty getProperty(@Nullable Direction direction) {
        if (direction == null){return CENTER;}
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
            default -> CENTER;
        };
    }
}
