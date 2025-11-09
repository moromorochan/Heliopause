package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class FluidSpreaderTowerBlock extends Block {

    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public FluidSpreaderTowerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TOP,true)
                .setValue(BOTTOM,true)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM,TOP);
    }

    public static VoxelShape BASE_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    public static VoxelShape TOP_SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(blockState.getValue(TOP)){
            return TOP_SHAPE;
        }else{
            return BASE_SHAPE;
        }
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isNatural) {
        super.onPlace(state, level, pos, oldState, isNatural);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos){
        boolean top_connect = (level.getBlockState(pos.above()).is(BlockRegistry.FLUID_SPREADER_TOWER.get()));
        boolean bottom_connect = (level.getBlockState(pos.below()).is(BlockRegistry.FLUID_SPREADER_TOWER.get()));
        return blockState.setValue(TOP,!top_connect).setValue(BOTTOM,!bottom_connect);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player,
                                 @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }
        //上面にインタラクトしたなら
        if(hit.getDirection()==Direction.UP){
            //上面が空気かどうか
            BlockPos abovePos = pos.above();
            if(level.getBlockState(abovePos).isAir()){
                if(heldItem.getItem() instanceof BlockItem blockItem){
                    Block block = blockItem.getBlock();
                    BlockState blockState = block.defaultBlockState();
                    //フルブロックかどうか
                    if(blockState.isCollisionShapeFullBlock(level,pos)){
                        level.setBlock(abovePos, BlockRegistry.FLUID_SPREADER_ORB.get().defaultBlockState(), 3);
                        //ブロックエンティティが存在するか確認
                        BlockEntity blockEntity = level.getBlockEntity(abovePos);
                        if (blockEntity instanceof FluidSpreaderOrbBlockEntity orbBlockEntity) {
                            //アイテムスタックを書き込む
                            orbBlockEntity.setCenterBlockState(blockState);
                            //手持ちを減らす
                            heldItem.shrink(1);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
