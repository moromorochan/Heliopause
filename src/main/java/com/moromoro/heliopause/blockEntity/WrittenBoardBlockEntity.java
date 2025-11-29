package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.EnumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.block.WrittenBoardBlock;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WrittenBoardBlockEntity extends BlockEntity {
    // 始端点からの最大距離
    public static final int PREVIEW_LIMIT_SIZE = 16;
    // 直線の親位置(始端点)
    private final List<BlockPos> linePairs = new ArrayList<>(); private static final String LINE_PAIR = "line_pair";
    // 円の親位置(中心)
    //private BlockPos CircleParentPos = worldPosition; private static final String CIRCLE_PARENT = "circle_parent";
    // ノード全体の起点の位置(原点)
    private BlockPos rootPos = worldPosition; private static final String ROOT = "root";
    // 属する種類
    //private WrittenBoardNodeType nodeType = WrittenBoardNodeType.ROOT; private static final String ATTRIBUTE = "attribute";

    // 自身を中心にする円の半径配列
    private final List<Double> circleRadii = new DoubleArrayList(); private static final String CIRCLES = "circles";

    public WrittenBoardBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.WRITTEN_BOARD_BE.get(), pos, blockState);
    }

    public WrittenBoardBlockEntity(BlockPos pos, BlockState blockState, BlockPos pairPos, String string){
        super(BlockEntityRegistry.WRITTEN_BOARD_BE.get(), pos, blockState);
        switch (string){
            case LINE_PAIR:
                this.linePairs.add(pairPos);
                break;
            case ROOT:
                this.rootPos = pairPos;
                break;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        //nbt.putLong(LINE_PAIR, this.linePairs.asLong());
        CompoundTag pairsTag = new CompoundTag();
        for (int i = 0; i < linePairs.size(); i++) {
            pairsTag.putLong(String.valueOf(i), linePairs.get(i).asLong());
        }
        nbt.put(LINE_PAIR, pairsTag);
        //nbt.putLong(CIRCLE_PARENT, this.CircleParentPos.asLong());
        nbt.putLong(ROOT, this.rootPos.asLong());
        //nbt.putString(ATTRIBUTE, this.nodeType.getSerializedName());
        CompoundTag radiiTag = new CompoundTag();
        for (int i = 0; i < circleRadii.size(); i++) {
            radiiTag.putDouble(String.valueOf(i), circleRadii.get(i));
        }
        nbt.put(CIRCLES,radiiTag);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        //this.linePairs = BlockPos.of(nbt.getLong(LINE_PAIR));
        linePairs.clear();
        CompoundTag pairsTag = nbt.getCompound(LINE_PAIR);
        for (int i = 0; i < pairsTag.size(); i++) {
            this.linePairs.add(BlockPos.of(pairsTag.getLong(String.valueOf(i))));
        }
        //this.CircleParentPos = BlockPos.of(nbt.getLong(CIRCLE_PARENT));
        this.rootPos = BlockPos.of(nbt.getLong(ROOT));
        //this.nodeType = WrittenBoardNodeType.valueOf(nbt.getString(ATTRIBUTE));
        circleRadii.clear();
        CompoundTag radiiTag = nbt.getCompound(CIRCLES);
        for (int i = 0; i < radiiTag.size(); i++) {
            this.circleRadii.add(radiiTag.getDouble(String.valueOf(i)));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
        this.load(nbt);
    }

    public List<BlockPos> getLinePairs() {
        return linePairs;
    }

    // 線分の描画
    public boolean setLinePairPos(BlockPos pairPos, boolean simulate) {
        if (level == null) {
            return false;
        }
        // 同じ線がないか確認
        if(this.linePairs.contains(pairPos)){
            return false;
        }
        // 黒板が揃っているか確認
        if(false){
            return false;
        }

        if(!simulate){
            // ペア側に適用
            if (level.getBlockEntity(pairPos) instanceof WrittenBoardBlockEntity pairEntity) {
                // ブロックエンティティがあるならパラメータを入れる
                pairEntity.addLinePair(worldPosition);
            } /*else {
                new WrittenBoardBlockEntity(pairPos,
                    BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                        .setValue(WrittenBoardBlock.CIRCLE_TYPE,WrittenBoardDrawType.DOUBLE_CIRCLE),
                    worldPosition, LINE_PAIR
                );
            }*/
            level.sendBlockUpdated(pairPos, level.getBlockState(pairPos), level.getBlockState(pairPos), 3);
            // 自身側に適用
            this.linePairs.add(pairPos);
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    private void addLinePair(BlockPos pairPos) {
        // 同じ線がないか確認
        if(this.linePairs.contains(pairPos)){
            return;
        }
        this.linePairs.add(pairPos);
        this.setChanged();
    }


    public BlockPos getRootPos() {
        return rootPos;
    }

    public void setRootPos(BlockPos rootPos) {
        this.rootPos = rootPos;
        this.setChanged();
        if(level != null){
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public List<Double> getCircleRadii() {
        return circleRadii;
    }

    // 円の描画
    public boolean setCircleRadius(double radius, boolean simulate){
        if(level == null){
            return false;
        }
        // 同じ半径がないか確認
        if(this.circleRadii.contains(radius)){
            return false;
        }
        // 隣の円と近すぎないか確認
        for (Double circleRadius : circleRadii) {
            if(Math.abs(radius - circleRadius) < 1){
                return false;
            }
        }
        // 黒板が揃っているか確認
        if(checkCircleBoard(worldPosition, radius, simulate)){
            if(!simulate){
                // 半径を登録
                this.circleRadii.add(radius);
                this.setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return true;
    }

    private boolean checkCircleBoard(BlockPos centerPos, double radius, boolean simulate) {
        if(level == null){
            return false;
        }
        // 最初の位置
        BlockPos firstPos = centerPos.offset((int)-Math.ceil(radius),0, (int)-Math.ceil(radius));
        for (int localX = 0; localX < radius*2 + 1; localX++) {
            for (int localZ = 0; localZ < radius*2 + 1; localZ++) {
                BlockPos localPos = firstPos.offset(localX, 0 ,localZ);
                // 距離の差
                double rangeDiff = Math.abs(localPos.getCenter().distanceTo(centerPos.getCenter()) - radius);
                // 範囲外はスキップ
                if(rangeDiff > 1){
                    continue;
                }
                // ブロックを取得
                BlockState localState = level.getBlockState(localPos);
                if(localState.is(BlockRegistry.BLACKBOARD.get()) || localState.is(BlockRegistry.WRITTEN_BOARD.get())){
                    // ピッタリの位置はノードを生成
                    if(!simulate && rangeDiff <= 0.001){
                        BlockState newState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                            .setValue(WrittenBoardBlock.CIRCLE_TYPE,WrittenBoardDrawType.DOUBLE_CIRCLE);
                        level.setBlock(localPos, newState, 3);
                    }
                }else {
                    return false;
                }
            }
        }
        // 円周上のノードを生成
        return true;
    }

}
