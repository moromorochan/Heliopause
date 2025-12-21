package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import com.moromoro.heliopause.EnumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrittenBoardBlock extends Block implements EntityBlock {
    public static final EnumProperty<WrittenBoardDrawType> CIRCLE_TYPE = WrittenBoardDrawType.create("type", WrittenBoardDrawType.class);
    public WrittenBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CIRCLE_TYPE, WrittenBoardDrawType.CROSS_CIRCLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CIRCLE_TYPE);
    }

    @Override
    public void onRemove(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState newState, boolean isMoving) {
        if (!(newState.getBlock() instanceof WrittenBoardBlock)) {
            if(level.getBlockEntity(blockPos) instanceof WrittenBoardBlockEntity entity){
                entity.eraseFromPos(blockPos.getCenter(), WrittenBoardBlockEntity.CLICK_SIZE);
                entity.eraseNode( 2);
            }
        }
        super.onRemove(blockState, level, blockPos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new WrittenBoardBlockEntity(blockPos, blockState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return BlockRegistry.BLACKBOARD.get().getCloneItemStack(state, target, level, pos, player);//super.getCloneItemStack(state, target, level, pos, player);
    }

    // 位置がノードサイズの判定内かどうか
    public static boolean checkPosInNode(BlockState state, BlockPos pos, Vec3 clickedPos) {

        // 判定サイズを取得
        WrittenBoardDrawType nodeType = state.getValue(WrittenBoardBlock.CIRCLE_TYPE);
        double NodeSize = WrittenBoardDrawType.getNodeSize(nodeType);

        // クリック位置をXZ平面で判定
        Vec3 difference = pos.getCenter().subtract(clickedPos);
        return (Math.abs(difference.x()) < NodeSize/2 && Math.abs(difference.z()) < NodeSize/2);
    }
}
