package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity.PREVIEW_LIMIT_SIZE;

public class BlackBoardBlock extends Block {
    //public static final IntegerProperty CoordinateX = IntegerProperty.create("coordinate_x",0,32);
    //public static final IntegerProperty CoordinateZ = IntegerProperty.create("coordinate_z",0,32);
    public BlackBoardBlock(Properties properties) {
        super(properties);
        //registerDefaultState(this.defaultBlockState().setValue(CoordinateX,16).setValue(CoordinateZ,16));
    }

    /*@Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(CoordinateX,CoordinateZ);
    }*/

    @Override
    public void onRemove(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState newState, boolean isMoving) {
        if (newState.getBlock() instanceof AbstractWrittenBoardBlock) {
            level.playSound(null, blockPos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 0.5f, 1.0f);
        } else {

            for (AbstractWrittenBoardBlockEntity node : getNodeList(level, blockPos)) {
                node.eraseFromPos(blockPos.getCenter(), (Math.sqrt(2)/2) + AbstractWrittenBoardBlockEntity.CLICK_SIZE);
                level.playSound(null, blockPos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            }

            //クリック位置からPREVIEW_LIMIT_SIZEまで走査
            /*Iterable<BlockPos.MutableBlockPos> localPosIterable = BlockPos.spiralAround(blockPos, PREVIEW_LIMIT_SIZE, Direction.NORTH, Direction.EAST);
            for (BlockPos.MutableBlockPos localPos : localPosIterable) {
                if (level.getBlockEntity(localPos) instanceof AbstractWrittenBoardBlockEntity entity) {
                    // ヒット判定
                    entity.eraseFromPos(blockPos.getCenter(), (Math.sqrt(2) / 2) + AbstractWrittenBoardBlockEntity.CLICK_SIZE);
                }
            }*/
        }
        super.onRemove(blockState, level, blockPos, newState, isMoving);
    }

    public static @NotNull List<AbstractWrittenBoardBlockEntity> getNodeList(@NotNull Level level, @NotNull BlockPos blockPos) {
        List<AbstractWrittenBoardBlockEntity> nodeList = new ArrayList<>();
        // 半径16ブロックを含む周囲9チャンクを走査
        for (int chunkOffsetX = -1; chunkOffsetX <= 1; chunkOffsetX++) {
            for (int chunkOffsetZ = -1; chunkOffsetZ <= 1; chunkOffsetZ++) {
                // チャンクを取得
                ChunkAccess chunk = level.getChunk(
                    new BlockPos(blockPos.getX() + chunkOffsetX * 16,0, blockPos.getZ() + chunkOffsetZ * 16)
                );
                // チャンクからブロックエンティティ取得
                for (BlockPos blockEntityPos : chunk.getBlockEntitiesPos()) {
                    // 高さが違うなら判定外
                    if(blockEntityPos.getY() != blockPos.getY()){
                        continue;
                    }
                    // 距離が遠いなら判定外
                    if(blockPos.getCenter().distanceToSqr(blockEntityPos.getCenter()) > PREVIEW_LIMIT_SIZE * PREVIEW_LIMIT_SIZE){
                        continue;
                    }
                    // 種類が合っているなら
                    if (level.getBlockEntity(blockEntityPos) instanceof AbstractWrittenBoardBlockEntity entity) {
                        // ヒット判定
                        nodeList.add(entity);
                    }
                }
            }
        }
        return nodeList;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // ルートノードの動作
        for (AbstractWrittenBoardBlockEntity boardBlockEntity : getNodeList(level, blockPos)) {
            if(!boardBlockEntity.isRoot(level)){
                continue;
            }
            if(boardBlockEntity.operateFromArea(level, blockPos, hitResult.getLocation(), player, hand)){
                return InteractionResult.SUCCESS;//InteractionResult.sidedSuccess(!level.isClientSide());
            }
        }

        return super.use(blockState, level, blockPos, player, hand, hitResult);
    }
}
