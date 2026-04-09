package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.block.AbstractWrittenBoardBlock;
import com.moromoro.heliopause.block.BlackBoardBlock;
import com.moromoro.heliopause.block.WrittenBoardBlock;
import com.moromoro.heliopause.item.CompassItem;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.render.WrittenBoardRenderer;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.*;

public abstract class AbstractWrittenBoardBlockEntity extends BlockEntity {
    // 始端点からの最大距離
    public static final int PREVIEW_LIMIT_SIZE = 16;
    public static final double CLICK_SIZE = 3.0/16;

    // 自身の親
    private BlockPos parentPos = worldPosition; public static final String PARENT_POS = "parent_pos";

    // 直線のペア位置
    private final List<BlockPos> linePairs = new ArrayList<>(); public static final String LINE_PAIRS = "line_pairs";
    // 円周から見た円の中心位置
    private final List<BlockPos> circleCenters = new ArrayList<>(); public static final String CIRCLE_CENTERS = "circle_centers";

    // 自身を中心にする円の半径配列
    private final List<Double> circleRadii = new DoubleArrayList(); public static final String CIRCLES = "circle_radii";

    public AbstractWrittenBoardBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public abstract ResourceLocation getLineType();
    public abstract ResourceLocation getCircleType();

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

    @Override
    public AABB getRenderBoundingBox() {
        // 円の範囲
        List<Double> renderCircleRadii = new ArrayList<>(getCircleRadii());
        List<BlockPos> renderPairPos = new ArrayList<>(getLinePairs());
        // 描きかけ描画があるならその範囲
        Minecraft instance = Minecraft.getInstance();
        if (instance.level != null) {
            // カメラエンティティを取得
            Entity cameraEntity = instance.getCameraEntity();
            if (cameraEntity instanceof Player player) {
                // アイテムを手に持ってるか確認
                ItemStack itemStack = player.getMainHandItem();
                if (itemStack.getItem() instanceof CompassItem compassItem) {
                    BlockPos firstPos = compassItem.getPosFromTag(itemStack.getTag());
                    if(firstPos!=null && firstPos.equals(worldPosition)){
                        // 位置を取得
                        Vec3 pointPos = WrittenBoardRenderer.getPointPos(worldPosition, player, 0);
                        if (pointPos != null) {
                            switch (compassItem.getSelectIndex(itemStack)) {
                                // コンパス・黒板消し
                                case 0, 3: {
                                    break;
                                }
                                // ビームコンパス・定規
                                case 1: {
                                    BlockPos pointBlockPos = BlockPos.containing(pointPos).below();

                                    double length = firstPos.getCenter().distanceTo(pointBlockPos.getCenter());
                                    renderCircleRadii.add(length);

                                }
                                case 2:{
                                    BlockPos pointBlockPos = BlockPos.containing(pointPos).below();

                                    renderPairPos.add(pointBlockPos);

                                }
                            }
                        }
                    }
                }
            }
        }
        double maxCircle = renderCircleRadii.isEmpty() ? 0.5 : Collections.max(renderCircleRadii) + CLICK_SIZE;
        double minPosX = worldPosition.getCenter().x() - maxCircle;
        double minPosZ = worldPosition.getCenter().z() - maxCircle;
        double maxPosX = worldPosition.getCenter().x() + maxCircle;
        double maxPosZ = worldPosition.getCenter().z() + maxCircle;
        // 線の範囲
        for (BlockPos linePair : renderPairPos) {
            minPosX = Math.min(minPosX, linePair.getX());
            minPosZ = Math.min(minPosZ, linePair.getZ());
            maxPosX = Math.max(maxPosX, linePair.getX()+1);
            maxPosZ = Math.max(maxPosZ, linePair.getZ()+1);
        }

        return new AABB(minPosX, worldPosition.getY(), minPosZ, maxPosX, worldPosition.getY() + 1.0, maxPosZ);
    }

    // 範囲からの動作 -----------------------------------------------------------

    public abstract boolean operateFromArea(Level level, BlockPos blockPos, Vec3 location, Player player, InteractionHand hand);

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
            // 破壊でない場合はブロック更新
            if(level.getBlockState(worldPosition).getBlock() instanceof AbstractWrittenBoardBlock) {
            BlockState newState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                .setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE, isFunctionalRoot(level) ? WrittenBoardDrawType.CROSS_CIRCLE : WrittenBoardDrawType.CHILD_NODE);
                changeCircleBoardBlock(level, worldPosition, newState);
            }
            //level.setBlock(worldPosition, newState,3);
            return;
        }
        // 円周に含まれるなら円を消す
        if(!getCircleCenters().isEmpty()){
            if (eraseLevel >= 1) {
                for (BlockPos centerPos : getCircleCenters()) {
                    if (level.getBlockEntity(centerPos) instanceof AbstractWrittenBoardBlockEntity centerEntity) {
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
        // 同心円がなく、繋がりがないならノードを消す あるいは全部消す
        if(eraseLevel >= 2 || getCircleRadii().isEmpty()){
            // 全消しの場合は円を消す
            for (double circleRadius : getCircleRadii()) {
                // 円を消す
                eraseCircle(level, this, circleRadius);
            }
            // 破壊でない場合はブロック更新
            if(level.getBlockState(worldPosition).getBlock() instanceof AbstractWrittenBoardBlock) {
                level.setBlock(worldPosition, BlockRegistry.BLACKBOARD.get().defaultBlockState(), 3);
            }
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
                    .setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE, WrittenBoardDrawType.CHILD_NODE), 3);
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
                if (!getBlockPos().equals(parentPos) && level.getBlockEntity(parentPos) instanceof AbstractWrittenBoardBlockEntity parentEntity) {
                    double radius = worldPosition.getCenter().distanceTo(parentPos.getCenter());
                    // 自身が円周に含まれているなら
                    if (parentEntity.getCircleRadii().contains(radius)) {
                        // 自身以外の円周上のノードを消す
                        for (BlockPos nodePos : getCircleLatticePos(parentPos, radius)) {
                            if(level.getBlockEntity(nodePos)instanceof AbstractWrittenBoardBlockEntity nodeEntity){
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
        // ノードサイズと比較
        double nodeDistance = Math.max(Math.abs(surfacePosition.x() - surfaceLocation.x()),Math.abs(surfacePosition.z() - surfaceLocation.z()));
        if(nodeDistance <= WrittenBoardDrawType.getNodeSize(this.getBlockState().getValue(WrittenBoardBlock.CIRCLE_TYPE))){
            eraseNode(2);
        }
        return status;
    }

    public void eraseFromPos(){
        eraseFromPos(worldPosition.getCenter(), CLICK_SIZE);
    }

    // 線との干渉確認
    private boolean checkCollidesToLine(Level worldLevel, BlockPos checkPos, double clickSize) {
        // 別の線に重ならないか確認
        for (AbstractWrittenBoardBlockEntity nodeEntity : BlackBoardBlock.getNodeList(worldLevel, checkPos)) {
            // 端点は考慮しない
            if(checkPos.equals(nodeEntity.getBlockPos())){
                continue;
            }
            List<BlockPos> linePairs = nodeEntity.getLinePairs();
            // 端点が一致する線は除外
            if(linePairs.contains(checkPos)){
                continue;
            }
            Vec3 surfacePosition = nodeEntity.getBlockPos().getCenter().multiply(1,0,1);
            Vec3 surfaceLocation = checkPos.getCenter().multiply(1,0,1);//clickLocation.multiply(1,0,1);
            // 距離を取得
            double distance = surfacePosition.distanceTo(surfaceLocation);

            // 重なる線があるならtrue
            if(!getLineFromPos(nodeEntity.getLinePairs(), clickSize, surfacePosition, surfaceLocation, distance).isEmpty()){
                return true;
            }
        }
        return false;
    }

    public static void changeCircleBoardBlock(Level level, BlockPos blockPos,@NotNull BlockState resultBlockState) {
        if(!level.isClientSide()){
            // 魔法陣ブロックなら陣のnbtを継承
            if(resultBlockState.getBlock() instanceof AbstractWrittenBoardBlock boardBlock){
                // nbtを保存
                CompoundTag nbt = new CompoundTag();
                CompoundTag circleNbt = new CompoundTag();
                if(level.getBlockEntity(blockPos) instanceof AbstractWrittenBoardBlockEntity blockEntity){
                    nbt = blockEntity.saveWithoutMetadata().copy();
                    // 陣の情報だけを保持
                        // ルート位置
                    circleNbt.putLong(PARENT_POS, nbt.getLong(PARENT_POS));
                        // 線分のペア
                    circleNbt.put(LINE_PAIRS, nbt.getCompound(LINE_PAIRS));
                        // 円の中心
                    circleNbt.put(CIRCLE_CENTERS, nbt.getCompound(CIRCLE_CENTERS));
                        // 円の半径
                    circleNbt.put(CIRCLES, nbt.getCompound(CIRCLES));
                }
                // ブロックを更新
                level.setBlock(blockPos, resultBlockState,3);

                // ブロックエンティティを取得
                BlockEntity newBlockEntity = Objects.requireNonNull(
                    boardBlock.newBlockEntity(blockPos, resultBlockState)
                ).getType().create(blockPos, resultBlockState);

                if(newBlockEntity != null) {
                    // nbtを反映
                    newBlockEntity.load(circleNbt);
                    // ワールドに反映
                    level.setBlockEntity(newBlockEntity);
                }
            }else{
                level.setBlock(blockPos, resultBlockState,3);
            }
        }
    }

    // 階層の制御 -----------------------------------------------------------

    public BlockPos getParentPos() {
        return parentPos;
    }

    public void setParentPos(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity, BlockPos parentPos) {
        // ループ回避
        if(worldlevel.getBlockEntity(parentPos) instanceof AbstractWrittenBoardBlockEntity parentEntity){
            BlockPos parentRoot = parentEntity.getRootPos(worldlevel, worldPosition);
            if(parentRoot.equals(worldPosition)){
                resetParentPos(worldlevel, this);
            }
        }

        entity.parentPos = parentPos;
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
    }

    public static void resetParentPos(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity){
        entity.parentPos = entity.getBlockPos();
        //entity.setParentPos(worldlevel, entity, entity.getBlockPos());
    }

    // parentを辿った一番根元を取得(ルートノードの位置)
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

        if(worldLevel.getBlockEntity(getParentPos()) instanceof AbstractWrittenBoardBlockEntity parentEntity){
            // 階層を辿る
            return parentEntity.getRootPos(worldLevel, startPos);
        }else{
            return worldPosition;
        }
    }

    // 自身がルートノードか確認
    public boolean isRoot(@NotNull Level worldlevel){
        BlockState blockState = this.getBlockState();
        if (blockState.getBlock() instanceof AbstractWrittenBoardBlock) {
            if (!WrittenBoardDrawType.isChildNode(blockState.getValue(AbstractWrittenBoardBlock.CIRCLE_TYPE))){
                return this.getRootPos(worldlevel, worldPosition).equals(worldPosition);
            }
        }
        return false;
    }

    // 自身が機能を持つルートノードか確認
    public boolean isFunctionalRoot(@NotNull Level worldlevel){
        return false;
    }

    // ルートノードのブロックエンティティ取得
    public @Nullable AbstractWrittenBoardBlockEntity getRootEntity(){
        if(level == null){
            return null;
        }
        if(level.getBlockEntity(getRootPos(level, worldPosition)) instanceof AbstractWrittenBoardBlockEntity entity){
            return entity;
        }
        return null;
    }

    // ネットワークへの追加・削除時の挙動
    protected void nodeNetworkChanged(){

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
        // 線を組み立て済みのネットワークに追加しようとしてないか確認
        if (checkUnnecessaryLine(level, pairPos)) {
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
    private static boolean addLinePairPos(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity, BlockPos pairPos, Boolean simulate) {
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
    private void eraseLine(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity, BlockPos pairPos) {
        // 相手のペア位置を消す
        if(worldlevel.getBlockEntity( pairPos) instanceof AbstractWrittenBoardBlockEntity pairEntity){
            pairEntity.linePairs.remove(worldPosition);
            // 親設定を更新
            if(pairEntity.getParentPos()==worldPosition){
                resetParentPos(worldlevel, pairEntity);
                AbstractWrittenBoardBlockEntity rootEntity = getRootEntity();
                if(rootEntity!= null){
                    rootEntity.nodeNetworkChanged();
                }
            }
            pairEntity.setChanged();
            worldlevel.sendBlockUpdated(pairEntity.getBlockPos(), pairEntity.getBlockState(), pairEntity.getBlockState(), 3);
        }
        // 自身のペア位置を消す
        entity.linePairs.remove(pairPos);
        // 繋がりが全てないなら、ノードを削除
        /*if(entity.linePairs.isEmpty() && entity.circleCenters.isEmpty() && entity.getParentPos().equals(entity.getBlockPos())){
            entity.setRemoved();
        }*/
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
    }

    // 線分のペア生成
    private void createPairNode(@NotNull Level worldLevel, BlockPos childPos) {
        if(!worldLevel.isClientSide){
            BlockState newState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                .setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE,WrittenBoardDrawType.CHILD_NODE);
            BlockEntity nodeEntity = worldLevel.getBlockEntity(childPos);

            // 接続元のネットワークが組み立て済みか確認
            boolean isInFunctionalNetwork;
            AbstractWrittenBoardBlockEntity rootEntity = getRootEntity();
            if(rootEntity!= null){
                isInFunctionalNetwork = rootEntity.isFunctionalRoot(worldLevel);
            } else {
                isInFunctionalNetwork = false;
            }
            // ブロックエンティティが無いなら生成する
            if (!(nodeEntity instanceof AbstractWrittenBoardBlockEntity blockEntity)) {
                worldLevel.setBlock(childPos, newState, 3);
                AbstractWrittenBoardBlockEntity newEntity = BlockEntityRegistry.WRITTEN_BOARD_BE.get().create(childPos,newState);
                if(newEntity != null){
                    worldLevel.setBlockEntity(newEntity);
                    addLinePairPos(worldLevel, newEntity, worldPosition, false);
                    // 組み立て済みのネットワークには追加しない
                    if(!isInFunctionalNetwork){
                        newEntity.setParentPos(worldLevel, newEntity, worldPosition);
                        if(rootEntity!= null) {
                            rootEntity.nodeNetworkChanged();
                        }
                    }
                    //newEntity.linePairs.add(worldPosition);//.setRootPos(this.getParentPos());
                }
            }else{
                addLinePairPos(worldLevel, blockEntity, worldPosition, false);
                if(!blockEntity.isRoot(worldLevel)){
                    worldLevel.setBlock(childPos, newState, 3);
                    // 既に別のネットワークに所属するものは追加しない 組み立て済みのネットワークには追加しない
                    if(blockEntity.getParentPos() == blockEntity.getBlockPos() && !isInFunctionalNetwork) {
                        blockEntity.setParentPos(worldLevel, blockEntity, worldPosition);//.linePairs.add(worldPosition);//.setRootPos(this.getParentPos());
                        if(rootEntity!= null) {
                            rootEntity.nodeNetworkChanged();
                        }
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
                BlockPos localPos = new BlockPos(localX, startPos.getY(), localZ);
                BlockState localBlockState = level.getBlockState(localPos);
                // 始端・終端位置
                if(
                    (localX == startPos.getX() && localZ == startPos.getZ())
                    ||(localX == pairPos.getX() && localZ == pairPos.getZ())
                ){
                    // 描かれた黒板なら接続処理
                    if(localBlockState.is(BlockRegistry.WRITTEN_BOARD.get())) {
                        continue;
                    }
                    // 黒板か確認
                    if (!localBlockState.is(BlockRegistry.BLACKBOARD.get())) {
                        return false;
                    }
                    // 別の線に重ならないか確認
                    if(checkCollidesToLine(level, localPos, CLICK_SIZE)){
                        return false;
                    }
                    /*for (AbstractWrittenBoardBlockEntity nodeEntity : BlackBoardBlock.getNodeList(level, localPos)) {
                        Vec3 surfacePosition = nodeEntity.getBlockPos().getCenter().multiply(1,0,1);
                        Vec3 surfaceLocation = localPos.getCenter().multiply(1,0,1);//clickLocation.multiply(1,0,1);
                        // 距離を取得
                        double distance = surfacePosition.distanceTo(surfaceLocation);

                        if(!getLineFromPos(nodeEntity.getLinePairs(), CLICK_SIZE, surfacePosition, surfaceLocation, distance).isEmpty()){
                            return false;
                        }
                    }*/
                    continue;
                }
                // 黒板なら問題なし
                if (localBlockState.is(BlockRegistry.BLACKBOARD.get())) {
                    continue;
                }
                // ローカル位置を線基準の座標に変換
                Vector2d posDiff = new Vector2d(localX, localZ).sub(startPos.getX(), startPos.getZ());
                // 外積で距離判定
                double localDistance = Math.abs(deltaX * posDiff.y() - deltaZ * posDiff.x())/length;

                // 描かれた黒板ならノードと重ならないか確認
                if(localBlockState.is(BlockRegistry.WRITTEN_BOARD.get())){
                    // 判定サイズを取得
                    //WrittenBoardDrawType nodeType = localBlockState.getValue(AbstractWrittenBoardBlock.CIRCLE_TYPE);
                    //double NodeSize = WrittenBoardDrawType.getNodeSize(nodeType);
                    // 重なる場合キャンセル
                    if(localDistance <= CLICK_SIZE){
                        return false;
                    }
                }
                // 描ける対象でないなら、重なる場合キャンセル
                else {
                    if(localDistance <= (Math.sqrt(2)/2)+CLICK_SIZE){
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // 線を組み立て済みのネットワークに追加しようとしてないか確認
    private boolean checkUnnecessaryLine(@NotNull Level worldLevel,BlockPos pairPos) {
        if(worldLevel.getBlockEntity(getRootPos(worldLevel, worldPosition)) instanceof AbstractWrittenBoardBlockEntity rootEntity){
            // 組み立て済みのネットワークか確認
            boolean isInFunctionalNetwork = rootEntity.isFunctionalRoot(worldLevel);
            if(isInFunctionalNetwork){
                if(worldLevel.getBlockEntity(pairPos) instanceof AbstractWrittenBoardBlockEntity pairEntity){
                    // ペアのネットワークが自身と同じならキャンセル
                    return this.getRootPos(worldLevel, worldPosition).equals(pairEntity.getRootPos(worldLevel, pairPos));
                }
            }
        }
        return false;
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

    private static void addCircleCenterPos(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity, BlockPos pairPos){
        if(entity.circleCenters.contains(pairPos)){
            return;
        }
        entity.circleCenters.add(pairPos);
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
    }

    private void removeCircleCenterPos(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity, BlockPos pairPos) {
        entity.circleCenters.remove(pairPos);
        // 親設定を更新
        if(entity.getParentPos().equals(pairPos)){
            resetParentPos(worldlevel, entity);
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

    public double getMaxCircleRadius() {
        if(circleRadii.isEmpty()){
            return 0;
        }
        return Collections.max(circleRadii);
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
        // 円を組み立て済みのネットワークに追加しようとしてないか確認
        if (checkUnnecessaryCircle(level)) {
            return false;
        }
        // 黒板が揃っているか確認
        if (!checkCircleBoard(worldPosition, radius, simulate)) {
            return false;
        }
        if(!simulate){
            // 半径を登録
            this.circleRadii.add(radius);
            AbstractWrittenBoardBlockEntity rootEntity = getRootEntity();
            if(rootEntity!= null){
                rootEntity.nodeNetworkChanged();
            }
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    private void eraseCircle(@NotNull Level worldlevel, AbstractWrittenBoardBlockEntity entity, double radius) {
        // 円周を消す
        entity.circleRadii.remove(radius);
        entity.setChanged();
        worldlevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);

        for(BlockPos nodePos : getCircleLatticePos(entity.getBlockPos(), radius)){
            // 円周上のノードを消す
            if(worldlevel.getBlockEntity(nodePos)instanceof AbstractWrittenBoardBlockEntity nodeEntity){
                nodeEntity.removeCircleCenterPos(worldlevel, nodeEntity, entity.getBlockPos());
                nodeEntity.eraseNode(0);
                // ネットワークに更新を伝える
                AbstractWrittenBoardBlockEntity rootEntity = getRootEntity();
                if(rootEntity!= null){
                    rootEntity.nodeNetworkChanged();
                }
            }
        }
    }

    // 黒板の確認
    private boolean checkCircleBoard(BlockPos centerPos, double radius, boolean simulate) {
        if(level == null){
            return false;
        }
        // ノード生成位置を確認
        for (BlockPos nodePos : getCircleLatticePos(centerPos, radius)) {
            if (checkCollidesToLine(level, nodePos, CLICK_SIZE)) {
                return false;
            }
        }

        // 最初の位置
        BlockPos firstPos = centerPos.offset((int)-Math.ceil(radius),0, (int)-Math.ceil(radius));
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
                if(level.getBlockEntity(localPos) instanceof AbstractWrittenBoardBlockEntity entity){
                    // 対象そのものがルートノードのとき
                    if(entity.isRoot(level)){
                        return false;
                    }
                    // 対象のルート位置がルートノード
                    BlockPos networkPos = entity.getRootPos(level, localPos);
                    if(level.getBlockEntity(networkPos) instanceof AbstractWrittenBoardBlockEntity networkRootEntity){
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

    // 円を組み立て済みのネットワークに追加しようとしてないか確認
    private boolean checkUnnecessaryCircle(@NotNull Level worldlevel){
        if(worldlevel.getBlockEntity(getRootPos(worldlevel, worldPosition)) instanceof AbstractWrittenBoardBlockEntity rootEntity){
            // 組み立て済みのネットワークか確認
            return rootEntity.isFunctionalRoot(worldlevel);
        }
        return false;
    }

    // 円周上の子ノード生成
    private void createChildNode(@NotNull Level worldLevel, BlockPos childPos) {
        BlockState newState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
            .setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE,WrittenBoardDrawType.CHILD_NODE);
        worldLevel.setBlock(childPos, newState, 3);
        if(!worldLevel.isClientSide){
            BlockEntity nodeEntity = worldLevel.getBlockEntity(childPos);
            // ブロックエンティティが無いなら生成する
            if (!(nodeEntity instanceof AbstractWrittenBoardBlockEntity blockEntity)) {
                AbstractWrittenBoardBlockEntity newEntity = BlockEntityRegistry.WRITTEN_BOARD_BE.get().create(childPos,newState);
                if(newEntity != null){
                    worldLevel.setBlockEntity(newEntity);
                    newEntity.setParentPos(worldLevel,newEntity,worldPosition);
                    addCircleCenterPos(worldLevel,newEntity,worldPosition);
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
                addCircleCenterPos(worldLevel,blockEntity,worldPosition);

            }
        }
    }

    // 円周から格子点(円周上のノード位置)を取得
    public static List<BlockPos> getCircleLatticePos(BlockPos centerPos, double radius){
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
