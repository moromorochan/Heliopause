package com.moromoro.heliopause.block;

import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWrittenBoardBlock extends BaseEntityBlock {
    public static final EnumProperty<WrittenBoardDrawType> CIRCLE_TYPE = WrittenBoardDrawType.create("type", WrittenBoardDrawType.class);
    public AbstractWrittenBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CIRCLE_TYPE);
    }

    @Override
    public void onRemove(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState newState, boolean isMoving) {
        if (!(newState.getBlock() instanceof AbstractWrittenBoardBlock)) {
            // 描かれたものを削除
            if(level.getBlockEntity(blockPos) instanceof AbstractWrittenBoardBlockEntity entity){
                //entity.eraseFromPos(blockPos.getCenter(), AbstractWrittenBoardBlockEntity.CLICK_SIZE);
                entity.eraseNode( 2);
                // 効果音を再生
                level.playSound(null, blockPos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }else{
            // 効果音を再生
            level.playSound(null, blockPos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 0.5f, 1.0f);
        }
        super.onRemove(blockState, level, blockPos, newState, isMoving);
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState);

    //public static double getNodeSize(BlockState blockState){return 0;}

    //public static boolean isChildNode(BlockState blockState){return false;}

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return BlockRegistry.BLACKBOARD.get().getCloneItemStack(state, target, level, pos, player);//super.getCloneItemStack(state, target, level, pos, player);
    }

    // 位置がノードサイズの判定内かどうか(マンハッタン)
    public static boolean checkPosInNode(BlockState state, BlockPos pos, Vec3 clickedPos) {

        // 判定サイズを取得
        WrittenBoardDrawType nodeType = state.getValue(AbstractWrittenBoardBlock.CIRCLE_TYPE);
        double NodeSize = WrittenBoardDrawType.getNodeSize(nodeType);

        // クリック位置をXZ平面で判定
        Vec3 difference = pos.getCenter().subtract(clickedPos);
        return (Math.abs(difference.x()) < NodeSize/2 && Math.abs(difference.z()) < NodeSize/2);
    }
}
