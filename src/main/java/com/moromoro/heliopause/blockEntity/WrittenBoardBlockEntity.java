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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.*;

public class WrittenBoardBlockEntity extends BlockEntity {
    // 始端点からの最大距離
    public static final int PREVIEW_LIMIT_SIZE = 16;
    public static final double CLICK_SIZE = 3.0/16;

    // 自身の親
    private BlockPos parentPos = worldPosition; private static final String PARENT_POS = "root_pos";

    // 直線のペア位置
    private final List<BlockPos> linePairs = new ArrayList<>(); private static final String LINE_PAIRS = "line_pairs";
    // 円周から見た円の中心位置
    private final List<BlockPos> circleCenters = new ArrayList<>(); private static final String CIRCLE_CENTERS = "circle_centers";

    // 自身を中心にする円の半径配列
    private final List<Double> circleRadii = new DoubleArrayList(); private static final String CIRCLES = "circle_radii";

    public WrittenBoardBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.WRITTEN_BOARD_BE.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        // ルート位置
        nbt.putLong(PARENT_POS, this.parentPos.asLong());

        // 線分のペア
        CompoundTag linesTag = new CompoundTag();
        for (int i = 0; i < linePairs.size(); i++) {
            linesTag.putLong(String.valueOf(i), linePairs.get(i).asLong());
        }
        nbt.put(LINE_PAIRS, linesTag);

        // 円の中心
        CompoundTag circlesTag = new CompoundTag();
        for (int i = 0; i < circleCenters.size(); i++) {
            circlesTag.putLong(String.valueOf(i), circleCenters.get(i).asLong());
        }
        nbt.put(CIRCLE_CENTERS, circlesTag);

        // 円の半径
        CompoundTag radiiTag = new CompoundTag();
        for (int i = 0; i < circleRadii.size(); i++) {
            radiiTag.putDouble(String.valueOf(i), circleRadii.get(i));
        }
        nbt.put(CIRCLES,radiiTag);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        // ルート位置
        this.parentPos = BlockPos.of(nbt.getLong(PARENT_POS));

        // 線分のペア
        linePairs.clear();
        CompoundTag linesTag = nbt.getCompound(LINE_PAIRS);
        for (int i = 0; i < linesTag.size(); i++) {
            this.linePairs.add(BlockPos.of(linesTag.getLong(String.valueOf(i))));
        }

        // 円の中心
        circleCenters.clear();
        CompoundTag circlesTag = nbt.getCompound(CIRCLE_CENTERS);
        for (int i = 0; i < circlesTag.size(); i++) {
            this.circleCenters.add(BlockPos.of(circlesTag.getLong(String.valueOf(i))));
        }

        // 円の半径
        circleRadii.clear();
        CompoundTag radiiTag = nbt.getCompound(CIRCLES);
        for (int i = 0; i < radiiTag.size(); i++) {
            this.circleRadii.add(radiiTag.getDouble(String.valueOf(i)));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
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

    @Override
    public void setRemoved() {
        //eraseDrawn(true, true);
        super.setRemoved();
    }

    // 描画制御(共通) -----------------------------------------------------------

    // ノードを消す 0: 繋がりが無ければノードを消す 1: 繋がりがあれば繋がりを消す 2: 繋がりもノードも消す
    public void eraseNode(int eraseLevel){
        if (level == null){
            return;
        }
        // ブロックが破壊されたときは、レベルを2に強制的に変更
        /*if(!level.getBlockState(worldPosition).is(BlockRegistry.WRITTEN_BOARD.get())){
            eraseLevel = 2;
        }*/
        // ルートノードなら、シンボルを解除
        if(eraseLevel == 1 && isRoot(level)){
            BlockState newState = this.getBlockState().setValue(WrittenBoardBlock.CIRCLE_TYPE, WrittenBoardDrawType.CHILD_NODE);
            level.setBlock(worldPosition, newState,3);
            return;
        }
        // 円周に含まれるなら円を消す
        if(!getCircleCenters().isEmpty()){
            if (eraseLevel >= 1) {
                for (BlockPos centerPos : getCircleCenters()) {
                    if (level.getBlockEntity(centerPos) instanceof WrittenBoardBlockEntity centerEntity) {
                        centerEntity.eraseFromPos(this.worldPosition.getCenter(), CLICK_SIZE);
                        return;
                    }
                }
            }
            if(eraseLevel <= 1){
                return;
            }
        }
        // 線があるなら線を消す
        if(!getLinePairs().isEmpty()){
            if (eraseLevel >= 1) {
                eraseFromPos();
            }
            if(eraseLevel <= 1){
                return;
            }
        }
        // 同心円がなく、繋がりがないならノードを消す
        if(eraseLevel >= 2 || getCircleRadii().isEmpty()){
            for (double circleRadius : getCircleRadii()) {
                eraseCircle(level, this, circleRadius);
            }
            level.setBlock(worldPosition, BlockRegistry.BLACKBOARD.get().defaultBlockState(), 3);
        }
    }
    /*public void eraseDrawn(Boolean eraseWithConnect, Boolean eraseWithCircle) {
        if(level == null){
            return;
        }
        BlockState blockState = level.getBlockState(worldPosition);
        boolean isWrittenBoard = blockState.is(BlockRegistry.WRITTEN_BOARD.get());

        // 同心円を持つなら
        if(!getCircleRadii().isEmpty()){
            if(!eraseWithConnect) {
                setParentPos(level, this, worldPosition);
                return;
            }
            // シンボルを消してノードに
            if(isWrittenBoard){
                level.setBlock(worldPosition, BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                    .setValue(WrittenBoardBlock.CIRCLE_TYPE, WrittenBoardDrawType.CHILD_NODE), 3);
            }
            // 繋がりを消す
            eraseLineConnect(level, this);
            // 円も消す場合
            if(eraseWithCircle){
                for(double radius : getCircleRadii()){

                    // 円周を消す
                    removeCircleRadius(level,this,radius);
                }
            }
        }
        // 線分を持つなら、ペアから自身を消す
        else if (!getLinePairs().isEmpty()) {
            if(!eraseWithConnect) {
                setParentPos(level, this, worldPosition);
                return;
            }
            // 繋がりを消す
            eraseLineConnect(level, this);
            // 自身を消す
            if(isWrittenBoard) {
                level.setBlock(worldPosition, BlockRegistry.BLACKBOARD.get().defaultBlockState(), 3);
            }
        }
        // 円周に含まれるなら、円を消す
        else if(!getCircleCenters().isEmpty() && eraseWithConnect){
            for(BlockPos parentPos : getCircleCenters()) {
                if (!getBlockPos().equals(parentPos) && level.getBlockEntity(parentPos) instanceof WrittenBoardBlockEntity parentEntity) {
                    double radius = worldPosition.getCenter().distanceTo(parentPos.getCenter());
                    // 自身が円周に含まれているなら
                    if (parentEntity.getCircleRadii().contains(radius)) {
                        // 自身以外の円周上のノードを消す
                        for (BlockPos nodePos : getCircleLatticePos(parentPos, radius)) {
                            if(level.getBlockEntity(nodePos)instanceof WrittenBoardBlockEntity nodeEntity){
                                nodeEntity.eraseDrawn(false, false);
                            }
                        }
                        // 円周を消す
                        parentEntity.removeCircleRadius(level,parentEntity,radius);
                    }else{
                        removeCircleCenterPos(level, this,parentPos);
                        if(getCircleCenters().isEmpty()){
                            level.setBlock(worldPosition, BlockRegistry.BLACKBOARD.get().defaultBlockState(), 3);
                            return;
                        }
                    }
                }
            }

        }
        // 繋がりがないなら、自身を消す
        else if(isWrittenBoard){
            level.setBlock(worldPosition, BlockRegistry.BLACKBOARD.get().defaultBlockState(), 3);
        }
    }*/

    // 線を消す
    public boolean eraseFromPos(Vec3 clickLocation, double clickSize) {
        if(level== null){
            return false;
        }
        boolean status = false;
        Vec3 surfacePosition = worldPosition.getCenter().multiply(1,0,1);
        Vec3 surfaceLocation = clickLocation.multiply(1,0,1);
        // 距離を取得
        double distance = surfacePosition.distanceTo(surfaceLocation);
        // 重なる円周を消す
        double selectRadius = getCircleFromPos(getCircleRadii() ,clickSize, distance);
        if (selectRadius != 0){
            this.eraseCircle(level, this, selectRadius);
            status = true;
        }

        List<BlockPos> selectQue = getLineFromPos(getLinePairs() ,clickSize, surfacePosition, surfaceLocation, distance);
        for (BlockPos pairPos : selectQue) {
            this.eraseLine(level, this, pairPos);
            status = true;
        }
        return status;
    }

    public void eraseFromPos(){
        eraseFromPos(worldPosition.getCenter(), CLICK_SIZE);
    }

    // 階層の制御 -----------------------------------------------------------

    public BlockPos getParentPos() {
        return parentPos;
    }

    public void setParentPos(@NotNull Level worldlevel, WrittenBoardBlockEntity entity, BlockPos parentPos) {
        // ループ回避
        if(worldlevel.getBlockEntity(parentPos) instanceof WrittenBoardBlockEntity parentEntity){
            BlockPos parentRoot = parentEntity.getRootPos(worldlevel, worldPosition);
            if(parentRoot.equals(worldPosition)){
                resetParentPos(worldlevel, this);
            }
        }

        entity.parentPos = parentPos;
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
    }

    public void resetParentPos(@NotNull Level worldlevel, WrittenBoardBlockEntity entity){
        entity.setParentPos(worldlevel, entity, entity.getBlockPos());
    }

    public BlockPos getRootPos(@NotNull Level worldLevel, BlockPos startPos){
        /*if(true)
        return worldPosition;*/
        // 自身が終端なら自身の位置を返す
        if(getParentPos().equals(worldPosition)){
            return worldPosition;
        }
        // ループしていたらオーバーフロー回避
        if(getParentPos().equals(startPos)){
            if(!worldLevel.isClientSide()) {
                resetParentPos(worldLevel, this);
            }
            return worldPosition;
        }

        if(worldLevel.getBlockEntity(getParentPos()) instanceof WrittenBoardBlockEntity parentEntity){
            // 階層を辿る
            return parentEntity.getRootPos(worldLevel, startPos);
        }else{
            return worldPosition;
        }
    }

    public boolean isRoot(@NotNull Level worldlevel){
        BlockState blockState = this.getBlockState();
        if (blockState.getBlock() instanceof WrittenBoardBlock) {
            if (!WrittenBoardDrawType.isChildNode(blockState.getValue(WrittenBoardBlock.CIRCLE_TYPE))){
                return this.getRootPos(worldlevel, worldPosition).equals(worldPosition);
            }
        }
        return false;
    }

    // 線の制御 -----------------------------------------------------------

    public static @NotNull List<BlockPos> getLineFromPos(List<BlockPos> linePairs, double clickSize, Vec3 surfacePosition, Vec3 surfaceLocation, double distance) {
        List<BlockPos> selectQue = new ArrayList<>();
        for(BlockPos pairPos : linePairs){
            Vec3 surfacePairPos = pairPos.getCenter().multiply(1,0,1);
            Vec3 lineVector = surfacePairPos.subtract(surfacePosition);
            Vec3 clickVector = surfaceLocation.subtract(surfacePosition);
            double lineLengthSquare = surfacePosition.distanceToSqr(surfacePairPos);
            if(lineLengthSquare == 0.0){
                if(distance < clickSize){
                    selectQue.add(pairPos);
                }
                continue;
            }
            double clickDot = clickVector.dot(lineVector);
            double distRatio = Math.max(0.0, Math.min(1.0, clickDot / lineLengthSquare));
            Vec3 closestPos = surfacePosition.add(lineVector.scale(distRatio));
            if(surfaceLocation.distanceToSqr(closestPos) < clickSize * clickSize){
                selectQue.add(pairPos);
            }
        }
        return selectQue;
    }

    public List<BlockPos> getLinePairs() {
        return linePairs;
    }

    // 線分の描画
    public boolean drawLine(BlockPos pairPos, boolean simulate) {
        if (level == null) {
            return false;
        }
        // 自身ではないか確認
        if(pairPos.equals(worldPosition)){
            return false;
        }
        // 長すぎないか確認
        if(worldPosition.getCenter().distanceTo(pairPos.getCenter()) > PREVIEW_LIMIT_SIZE){
            return false;
        }
        // 同じ線がないか確認
        if(!addLinePairPos(level,this, pairPos, true)){
            return false;
        }
        // 黒板が揃っているか確認
        if(!checkLineBoard(worldPosition, pairPos)){
            return false;
        }

        if(!simulate){
            // ペア側に適用
            createPairNode(level,pairPos);
            // 自身側に適用
            addLinePairPos(level,this, pairPos, false);
        }
        return true;
    }

    // 線分を線分の途中から描画
    /*public boolean drawLineFromPos(BlockPos clickLocation, double clickSize, boolean simulate ){
        // クリック位置
        Vec3 surfacePosition = worldPosition.getCenter().multiply(1,0,1);
        Vec3 surfaceLocation = clickLocation.getCenter().multiply(1,0,1);
        // 距離を取得
        double distance = surfacePosition.distanceTo(surfaceLocation);
        // 線があるか確認
        getLineFromPos(this.getLinePairs(), clickSize, surfacePosition, surfaceLocation, distance);
        // 線が交点を通るか確認
        getLineLatticePos();
    }*/

    // 線分の分割
    /*public void separateLineFromPos(){

    }*/

    // ペア位置の追加
    private boolean addLinePairPos(@NotNull Level worldlevel, WrittenBoardBlockEntity entity, BlockPos pairPos, Boolean simulate) {
        // 既にあるなら追加しない
        if(entity.linePairs.contains(pairPos)){
            return false;
        }
        if(!simulate){
            entity.linePairs.add(pairPos);
            entity.setChanged();
            worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
        }
        return true;
    }

    // 線分の削除 ペアの解除
    private void eraseLine(@NotNull Level worldlevel, WrittenBoardBlockEntity entity, BlockPos pairPos) {
        // 相手のペア位置を消す
        if(worldlevel.getBlockEntity( pairPos) instanceof WrittenBoardBlockEntity pairEntity){
            pairEntity.linePairs.remove(worldPosition);
            // 親設定を更新
            if(pairEntity.getParentPos()==worldPosition){
                pairEntity.resetParentPos(worldlevel, pairEntity);
            }
            pairEntity.setChanged();
        }
        // 自身のペア位置を消す
        entity.linePairs.remove(pairPos);
        // 繋がりが全てないなら、ノードを削除
        /*if(entity.linePairs.isEmpty() && entity.circleCenters.isEmpty() && entity.getParentPos().equals(entity.getBlockPos())){
            entity.setRemoved();
        }*/
        entity.setChanged();
        //worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
    }

    // 線分のペア生成
    private void createPairNode(@NotNull Level worldLevel, BlockPos childPos) {
        if(!worldLevel.isClientSide){
            BlockState newState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                .setValue(WrittenBoardBlock.CIRCLE_TYPE,WrittenBoardDrawType.CHILD_NODE);
            BlockEntity nodeEntity = worldLevel.getBlockEntity(childPos);
            // ブロックエンティティが無いなら生成する
            if (!(nodeEntity instanceof WrittenBoardBlockEntity blockEntity)) {
                worldLevel.setBlock(childPos, newState, 3);
                WrittenBoardBlockEntity newEntity = BlockEntityRegistry.WRITTEN_BOARD_BE.get().create(childPos,newState);
                if(newEntity != null){
                    worldLevel.setBlockEntity(newEntity);
                    newEntity.addLinePairPos(worldLevel, newEntity, worldPosition, false);
                    newEntity.setParentPos(worldLevel, newEntity, worldPosition);
                    //newEntity.linePairs.add(worldPosition);//.setRootPos(this.getParentPos());
                }
            }else{
                blockEntity.addLinePairPos(worldLevel, blockEntity, worldPosition, false);
                if(!blockEntity.isRoot(worldLevel)){
                    worldLevel.setBlock(childPos, newState, 3);
                    if(blockEntity.getParentPos() == blockEntity.getBlockPos()) {
                        blockEntity.setParentPos(worldLevel, blockEntity, worldPosition);//.linePairs.add(worldPosition);//.setRootPos(this.getParentPos());
                    }
                }
            }
        }
    }

    // 黒板の確認
    private boolean checkLineBoard(BlockPos startPos, BlockPos pairPos) {
        if (level == null) {
            return false;
        }
        // 線でないなら判定しない
        if(startPos.equals(pairPos)){
            return false;
        }
        // ブロック高さ判定
        int fixedY = startPos.getY();
        if (pairPos.getY() != fixedY) {
            return false;
        }

        // 外積で距離を計算
        double deltaX = pairPos.getX() - startPos.getX();
        double deltaZ = pairPos.getZ() - startPos.getZ();
        double length = new Vector2d(deltaX, deltaZ).length();

        Vector2i minPos = new Vector2i(Math.min(startPos.getX(), pairPos.getX()),Math.min(startPos.getZ(), pairPos.getZ()));
        Vector2i maxPos = new Vector2i(Math.max(startPos.getX(), pairPos.getX()),Math.max(startPos.getZ(), pairPos.getZ()));

        // 範囲
        for (int localX = minPos.x(); localX <= maxPos.x(); localX++) {
            for (int localZ = minPos.y(); localZ <= maxPos.y(); localZ++) {
                // ローカル位置を線基準の座標に変換
                Vector2d posDiff = new Vector2d(localX, localZ).sub(startPos.getX(), startPos.getZ());
                // 外積で距離判定
                double localDistance = Math.abs(deltaX * posDiff.y() - deltaZ * posDiff.x())/length;
                if(localDistance <= (Math.sqrt(2)/2)+CLICK_SIZE){
                    BlockPos localPos = new BlockPos(localX, startPos.getY(), localZ);
                    BlockState localBlockState = level.getBlockState(localPos);
                    if(!localBlockState.is(BlockRegistry.BLACKBOARD.get()) && !localBlockState.is(BlockRegistry.WRITTEN_BOARD.get())){
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // 円の制御 -----------------------------------------------------------

    public static double getCircleFromPos(List<Double> circleRadii, double clickSize, double distance){
        for(double radius : circleRadii){
            if(Math.abs(radius - distance) < clickSize){
                return radius;
            }
        }
        return 0;
    }

    public List<BlockPos> getCircleCenters() {
        return circleCenters;
    }

    private boolean addCircleCenterPos(@NotNull Level worldlevel, WrittenBoardBlockEntity entity, BlockPos pairPos, Boolean simulate){
        if(entity.circleCenters.contains(pairPos)){
            return false;
        }
        if(!simulate){
            entity.circleCenters.add(pairPos);
            entity.setChanged();
            worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
        }
        return true;
    }

    private void removeCircleCenterPos(@NotNull Level worldlevel, WrittenBoardBlockEntity entity, BlockPos pairPos) {
        entity.circleCenters.remove(pairPos);
        // 親設定を更新
        if(entity.getParentPos()==pairPos){
            entity.resetParentPos(worldlevel, entity);
        }
        // 繋がりが全てないなら、ノードを削除
        if(entity.linePairs.isEmpty() && entity.circleCenters.isEmpty() && entity.getParentPos().equals(entity.getBlockPos())){
            entity.setRemoved();
        }
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
    }

    public List<Double> getCircleRadii() {
        return circleRadii;
    }

    // 円の描画
    public boolean drawCircle(double radius, boolean simulate){
        if(level == null){
            return false;
        }
        // 小さすぎないか確認
        if(radius < 1){
            return false;
        }
        // 大きすぎないか確認
        if(radius > PREVIEW_LIMIT_SIZE){
            return false;
        }
        // 同じ半径がないか確認
        if(this.circleRadii.contains(radius)){
            return false;
        }
        // 隣の円と近すぎないか確認
        for (Double circleRadius : circleRadii) {
            if(Math.abs(radius - circleRadius) < 0.5){
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

    private void eraseCircle(@NotNull Level worldlevel, WrittenBoardBlockEntity entity, double radius) {
        // 円周を消す
        entity.circleRadii.remove(radius);
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);

        for(BlockPos nodePos : getCircleLatticePos(entity.getBlockPos(), radius)){
            // 円周上のノードを消す
            if(worldlevel.getBlockEntity(nodePos)instanceof WrittenBoardBlockEntity nodeEntity){
                nodeEntity.removeCircleCenterPos(worldlevel, nodeEntity, entity.getBlockPos());
                nodeEntity.eraseNode(0);
            }
        }
    }

    // 黒板の確認
    private boolean checkCircleBoard(BlockPos centerPos, double radius, boolean simulate) {
        if(level == null){
            return false;
        }
        // 最初の位置
        BlockPos firstPos = centerPos.offset((int)-Math.ceil(radius),0, (int)-Math.ceil(radius));
        // ノード生成キュー
        //List<BlockPos> nodeQue = new ArrayList<>();
        for (int localX = 0; localX < (radius+CLICK_SIZE)*2; localX++) {
            for (int localZ = 0; localZ < (radius+CLICK_SIZE)*2; localZ++) {
                BlockPos localPos = firstPos.offset(localX, 0 ,localZ);
                // 自身はスキップ
                if(localPos.equals(centerPos)){
                    continue;
                }
                // 距離の差
                double rangeDiff = /*Math.abs*/(localPos.getCenter().distanceTo(centerPos.getCenter()) - radius);
                // 範囲外はスキップ
                if(rangeDiff > 1){
                    continue;
                }
                // ブロックを取得
                BlockState localState = level.getBlockState(localPos);
                // 別のネットワークのノードがあれば描画キャンセル
                if(level.getBlockEntity(localPos) instanceof WrittenBoardBlockEntity entity){
                    // 対象そのものがルートノードのとき
                    if(entity.isRoot(level)){
                        return false;
                    }
                    // 対象のルート位置がルートノード
                    BlockPos networkPos = entity.getRootPos(level, localPos);
                    if(level.getBlockEntity(networkPos) instanceof WrittenBoardBlockEntity networkRootEntity){
                        if(networkRootEntity.isRoot(level)){

                            // 対象のルート位置と自身のルート位置が異なるとき
                            if(!networkPos.equals(this.getRootPos(level, worldPosition))){
                                return false;
                            }
                        }
                    }
                }
                if (!localState.is(BlockRegistry.BLACKBOARD.get()) && !localState.is(BlockRegistry.WRITTEN_BOARD.get())) {
                    return false;
                }
            }
        }
        if(!simulate){
            for (BlockPos nodePos : getCircleLatticePos(centerPos, radius)) {
                // 円周上のノードを生成
                createChildNode(level, nodePos);
                // 円周上のノードをペアに登録
                //this.circleCenters.add(localPos);
            }
        }
        return true;
    }

    // 円周上の子ノード生成
    private void createChildNode(@NotNull Level worldLevel, BlockPos childPos) {
        BlockState newState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
            .setValue(WrittenBoardBlock.CIRCLE_TYPE,WrittenBoardDrawType.CHILD_NODE);
        worldLevel.setBlock(childPos, newState, 3);
        if(!worldLevel.isClientSide){
            BlockEntity nodeEntity = worldLevel.getBlockEntity(childPos);
            // ブロックエンティティが無いなら生成する
            if (!(nodeEntity instanceof WrittenBoardBlockEntity blockEntity)) {
                WrittenBoardBlockEntity newEntity = BlockEntityRegistry.WRITTEN_BOARD_BE.get().create(childPos,newState);
                if(newEntity != null){
                    worldLevel.setBlockEntity(newEntity);
                    newEntity.setParentPos(worldLevel,newEntity,worldPosition);
                    newEntity.addCircleCenterPos(worldLevel,newEntity,worldPosition, false);
                }
            }else{
                // ルートなら
                if(blockEntity.getParentPos().equals(childPos)) {
                    // 循環参照を回避
                    /*if(childPos.equals(getRootPos(worldLevel))){
                        return;
                    }*/
                    blockEntity.setParentPos(worldLevel,blockEntity,worldPosition);
                }
                blockEntity.addCircleCenterPos(worldLevel,blockEntity,worldPosition,false);

            }
        }
    }

    // 円周から格子点を取得
    private List<BlockPos> getCircleLatticePos(BlockPos centerPos, double radius){
        double radiusSquare = radius * radius;

        Set<BlockPos> nodeSet = new LinkedHashSet<>();

        for (int searchX = (int) Math.floor(-radius); searchX <= 0; searchX++) {
            double remainSquare = radiusSquare - searchX * searchX;
            if (remainSquare < 0.0) continue;

            // zを確認
            double remain = Math.sqrt(remainSquare);
            int searchZ = (int) Math.round(remain);
            if(Math.abs(remain - searchZ) > 1e-9){
                continue;
            }

            // 組み合わせを全て入れる
            nodeSet.add(new BlockPos(-searchX, 0, +searchZ).offset(centerPos));
            nodeSet.add(new BlockPos(+searchX, 0, +searchZ).offset(centerPos));
            nodeSet.add(new BlockPos(-searchX, 0, -searchZ).offset(centerPos));
            nodeSet.add(new BlockPos(+searchZ, 0, +searchX).offset(centerPos));
            nodeSet.add(new BlockPos(+searchZ, 0, -searchX).offset(centerPos));
            nodeSet.add(new BlockPos(-searchZ, 0, +searchX).offset(centerPos));
            nodeSet.add(new BlockPos(-searchZ, 0, -searchX).offset(centerPos));
            nodeSet.add(new BlockPos(+searchX, 0, -searchZ).offset(centerPos));

        }
        return new ArrayList<>(nodeSet);
    }
}
