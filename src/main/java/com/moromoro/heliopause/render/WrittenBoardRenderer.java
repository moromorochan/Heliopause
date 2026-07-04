package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.block.AbstractWrittenBoardBlock;
import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import com.moromoro.heliopause.item.CompassItem;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;


public class WrittenBoardRenderer<T extends AbstractWrittenBoardBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;
    private static final float ARROW_PADDING = 7.5f/16f;
    private static final ResourceLocation DEFAULT_CIRCLE = CustomModelRegistry.CIRCLE_DEFAULT;//DECOR_MODELS.get("circle_default");
    private static final ResourceLocation DOTTED_CIRCLE = CustomModelRegistry.CIRCLE_DOTTED;
    //private final BakedModel defaultCircleModel;
    private static final ResourceLocation DEFAULT_LINE = CustomModelRegistry.LINE_DEFAULT;//DECOR_MODELS.get("line_default");
    private static final ResourceLocation DOTTED_LINE = CustomModelRegistry.LINE_DOTTED;//DECOR_MODELS.get("line_dotted");
    private static final ResourceLocation DEFAULT_ARROW = CustomModelRegistry.ARROW_DEFAULT;//DECOR_MODELS.get("arrow_default");
        //private final BakedModel defaultLineModel;
    public WrittenBoardRenderer(BlockEntityRendererProvider.Context context){
        super();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        //defaultCircleModel = Minecraft.getInstance().getModelManager().getModel(DEFAULT_CIRCLE);
        //defaultLineModel = Minecraft.getInstance().getModelManager().getModel(DEFAULT_LINE);
    }

    @Override
    public void render(@NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // ブロックの位置
        BlockPos entityPos = entity.getBlockPos();
        Minecraft instance = Minecraft.getInstance();
        if(instance.level == null){return;}
        BlockPos rootPos = entity.getRootPos(instance.level, entityPos);
        // デバッグカラー
        float[] debugColor = {1f,1f,1f};
        if(instance.options.renderDebug){
            combinedLight = 0xF000F0;
            debugColor[0] = RandomSource.create(rootPos.getX()).nextInt(0,255)/255f;/*(rootPos.getX() % 16 + 16) / 32f*/
            debugColor[1] = RandomSource.create(rootPos.getY()).nextInt(0,255)/255f;//(rootPos.getY() % 16 + 16) / 32f;
            debugColor[2] = RandomSource.create(rootPos.getZ()).nextInt(0,255)/255f;//(rootPos.getZ() % 16 + 16) / 32f;
            // ノードにオーバーレイ
            renderDebugNode(poseStack, bufferSource, combinedOverlay, debugColor);
        }

        //VertexConsumer cutoutBuffer = bufferSource.getBuffer(RenderType.cutout());

        // ヒットボックスと書きかけの描画
        // カメラエンティティを取得
        Entity cameraEntity = instance.getCameraEntity();
        if (cameraEntity instanceof Player player) {
            // アイテムを手に持ってるか確認
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof CompassItem compassItem) {
                // 位置を取得
                Vec3 pointPos = getPointPos(entityPos, player, partialTicks);
                if(pointPos != null){
                    // ノードの選択描画
                    renderNodeHitBox(poseStack, bufferSource, instance, entityPos, entity, pointPos);

                    BlockPos pointBlockPos = BlockPos.containing(pointPos).below();

                    switch (compassItem.getSelectIndex(itemStack)){
                        // コンパス
                        case 0:{
                            break;
                        }
                        // ビームコンパス
                        case 1:{
                            // 書きかけの描画
                            BlockPos firstPos = compassItem.getPosFromTag(itemStack.getTag());
                            if(firstPos!= null && firstPos.equals(entityPos)){
                                double length = firstPos.getCenter().distanceTo(pointBlockPos.getCenter());
                                float[] renderColor = new float[]{0.5f,0.5f,1.0f};
                                if(!entity.drawCircle(length, true)){
                                    renderColor = new float[]{1.0f,0.5f,0.5f};
                                    renderCircle(poseStack, bufferSource, blockRenderer, 0xF000F0, combinedOverlay, renderColor, instance, entityPos, length, DOTTED_CIRCLE);
                                }else{
                                    renderCircle(poseStack, bufferSource, blockRenderer, 0xF000F0, combinedOverlay, renderColor, instance, entityPos, length, DEFAULT_CIRCLE);
                                }
                                renderDebugNode(poseStack,bufferSource, combinedOverlay, renderColor);
                                for (BlockPos nodePos : AbstractWrittenBoardBlockEntity.getCircleLatticePos(firstPos, length)) {
                                    renderDebugNode(poseStack,bufferSource, combinedOverlay, renderColor, entityPos, nodePos);
                                }
                            }
                            break;
                        }
                        // 定規
                        case 2:{
                            // 書きかけの描画
                            BlockPos firstPos = compassItem.getPosFromTag(itemStack.getTag());
                            if(firstPos!= null && firstPos.equals(entityPos)){
                                float[] renderColor = new float[]{0.5f,0.5f,1.0f};
                                // 角度を計算
                                float angle = Math.atan2(firstPos.getZ() - pointBlockPos.getZ(), pointBlockPos.getX() - firstPos.getX());
                                if(!entity.drawLine(pointBlockPos, true)){
                                    renderColor = new float[]{1.0f,0.5f,0.5f};
                                    renderLine(poseStack, bufferSource, blockRenderer, 0xF000F0, combinedOverlay, renderColor, instance, firstPos, pointBlockPos, angle, DOTTED_LINE, null);
                                }else{
                                    renderLine(poseStack, bufferSource, blockRenderer, 0xF000F0, combinedOverlay, renderColor, instance, firstPos, pointBlockPos, angle, DEFAULT_LINE, null);
                                }
                                renderDebugNode(poseStack,bufferSource, combinedOverlay, renderColor);
                                renderDebugNode(poseStack,bufferSource, combinedOverlay, renderColor, entityPos, pointBlockPos);
                            }
                            break;
                        }
                        // 黒板消し
                        case 3:{
                            // 円の選択描画
                            renderCircleHitBox(poseStack, bufferSource, instance, entityPos, entity, pointPos);
                            // 線の選択描画
                            renderLineHitBox(poseStack, bufferSource, instance, entityPos, entity, pointPos);
                            break;
                        }
                    }
                }
            }
        }

        ResourceLocation circleType = DEFAULT_CIRCLE;
        ResourceLocation lineType = DEFAULT_LINE;
        // 描画の種類を取得
        if(instance.level.getBlockEntity(rootPos) instanceof AbstractWrittenBoardBlockEntity rootEntity){
            circleType = rootEntity.getCircleType();
            lineType = rootEntity.getLineType();
        }

        // 同心円の描画
        for (double circleRadius : entity.getCircleRadii()) {
            renderCircle(poseStack, bufferSource, blockRenderer, combinedLight, combinedOverlay, debugColor, instance, entityPos, circleRadius, circleType);
        }

        // 線分の描画
        for (BlockPos pairPos : entity.getLinePairs()) {
            // 相手のブロックエンティティ
            Level level = entity.getLevel();
            if (level != null && level.getBlockEntity(pairPos) instanceof AbstractWrittenBoardBlockEntity pairEntity) {
                // 角度を計算
                float angle = Math.atan2(entityPos.getZ() - pairPos.getZ(), pairPos.getX() - entityPos.getX());
                //ネットワークが同じ場合
                if(pairEntity.getRootPos(level, pairPos).equals(entity.getRootPos(level, entityPos))){
                    // 実線
                    if (!(angle <= 0)) {
                        renderLine(poseStack, bufferSource, blockRenderer, combinedLight, combinedOverlay, debugColor, instance, entityPos, pairPos, angle, lineType, null);
                    }
                }
                // 相手が親ではないなら描画
                else if (!pairEntity.isRoot(level)) {
                    // 親と子の場合
                    if(entity.isRoot(level)){
                        // 矢印
                        renderLine(poseStack, bufferSource, blockRenderer, combinedLight, combinedOverlay, debugColor, instance, entityPos, pairPos, angle, DEFAULT_LINE, DEFAULT_ARROW);
                    }
                    // 子同士の場合
                    else if(angle > 0){
                        // 線・デバッグカラー無し
                        renderLine(poseStack, bufferSource, blockRenderer, combinedLight, combinedOverlay, new float[]{1f, 1f, 1f}, instance, entityPos, pairPos, angle, DOTTED_LINE, null);
                    }
                }else{
                    // 親同士の場合
                    if(entity.isRoot(level)){
                        // 線・デバッグカラー無し
                        renderLine(poseStack, bufferSource, blockRenderer, combinedLight, combinedOverlay, new float[]{1f, 1f, 1f}, instance, entityPos, pairPos, angle, DOTTED_LINE, null);
                    }
                }
            }
        }
    }

    // 視線の交差位置を取得
    public static @Nullable Vec3 getPointPos(BlockPos entityPos, Player playerEntity, float partialTicks) {

        // 面の高さ
        double surfaceHeight = entityPos.getCenter().y + 0.5;

        // 視線を取得
        Vec3 eyePos = playerEntity.getEyePosition(partialTicks);
        Vec3 lookVec = playerEntity.getViewVector(partialTicks);
        // 目線が面より下なら or 目線が上向き~平行なら スキップ
        if((eyePos.y < surfaceHeight) || (lookVec.y >= 0)){
            return null;
        }

        // 面までの長さ
        double lookLength = (surfaceHeight - eyePos.y) / lookVec.y;
        // 交点が視線より後ろか、遠すぎるならスキップ
        if (lookLength < 0 || lookLength > playerEntity.getBlockReach()) {
            return null;
        }
        // 交点座標
        return eyePos.add(lookVec.scale(lookLength));
    }

    /*@Override
    public boolean shouldRender(@NotNull T entity, Vec3 viewPosition) {
        BlockPos entityPos = entity.getBlockPos();
        Minecraft instance = Minecraft.getInstance();
        if(instance.level == null){return false;}
        // 描きかけ表示があるなら常に描画
        // カメラエンティティを取得
        Entity cameraEntity = instance.getCameraEntity();
        if (cameraEntity instanceof Player player) {
            // アイテムを手に持ってるか確認
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.getItem() instanceof CompassItem compassItem) {
                // 位置を取得
                Vec3 pointPos = getPointPos(entityPos, player, 0);
                if(pointPos != null){
                    switch (compassItem.getSelectIndex(itemStack)) {
                        // コンパス・黒板消し
                        case 0,3: {
                            break;
                        }
                        // ビームコンパス・定規
                        case 1,2:{
                            return true;
                        }
                    }
                }
            }
        }

        return BlockEntityRenderer.super.shouldRender(entity, viewPosition);
    }*/

    // ノードのデバッグ描画
    private void renderDebugNode(PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay, float[] renderColor, @Nullable BlockPos entityPos, @Nullable BlockPos nodePos) {
        poseStack.pushPose();
        poseStack.translate(0.5f,1f,0.5f);
        if(entityPos != null && nodePos != null){
            Vec3 posDiff = nodePos.getCenter().subtract(entityPos.getCenter());
            poseStack.translate(posDiff.x(),posDiff.y(),posDiff.z());
        }
        //AABB debugCube = new AABB(6.5f/16, 17f/16, 6.5f/16, 9.5f/16, 21f/16, 9.5f/16);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugQuads());

        // 色
        Matrix4f mat = poseStack.last().pose();
        int r = (int) (Math.clamp(renderColor[0], 0f, 1f) * 255);
        int g = (int) (Math.clamp(renderColor[1], 0f, 1f) * 255);
        int b = (int) (Math.clamp(renderColor[2], 0f, 1f) * 255);

        // 座標
        float widthHalf = 3.5f/16;
        float height = 0.15f/16;

        //for (float[][] face : faces) {
        float[][] face = {{-widthHalf, height, 0}, {0 , height,-widthHalf}, {widthHalf, height,0}, {0, height,widthHalf}};
            for (int v = 0; v < 4; v++) {
                float[] vert = face[v];
                buffer.vertex(mat, vert[0], vert[1], vert[2])
                    .color(r, g, b, 255)
                    .overlayCoords(combinedOverlay)
                    .normal(0,0,0)
                    .endVertex();
            }
        //}

        poseStack.popPose();
    }

    private void renderDebugNode(PoseStack poseStack, MultiBufferSource bufferSource, int combinedOverlay, float[] renderColor){
        renderDebugNode(poseStack,bufferSource, combinedOverlay, renderColor, null, null);
    }

    private void renderNodeHitBox(PoseStack poseStack, MultiBufferSource bufferSource, Minecraft instance, BlockPos entityPos, AbstractWrittenBoardBlockEntity entity, Vec3 pointPos) {
        if(instance.level == null){
            return;
        }
        // ヒット位置がノード判定内なら
        BlockState hitBlockState = instance.level.getBlockState(BlockPos.containing(pointPos.add(0,-0.5,0)));
        if(hitBlockState.getBlock() instanceof AbstractWrittenBoardBlock && AbstractWrittenBoardBlock.checkPosInNode(entity.getBlockState(), entityPos, pointPos)) {//TODO: 大きいノードの範囲内に別のノードがあるときの対応
            float halfNodeSize = (float) (WrittenBoardDrawType.getNodeSize(hitBlockState.getValue(AbstractWrittenBoardBlock.CIRCLE_TYPE)) /2);

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINES);
            poseStack.pushPose();
            poseStack.translate(0.5,1.0,0.5);

            // 現在の行列を取得
            Matrix4f matrix = poseStack.last().pose();

            // ローカル空間での 8 頂点（長手方向 = +X）
            Vector3f[] corners = new Vector3f[] {
                new Vector3f(-halfNodeSize, 0, -halfNodeSize),
                new Vector3f( halfNodeSize, 0, -halfNodeSize),
                new Vector3f( halfNodeSize, 0,  halfNodeSize),
                new Vector3f(-halfNodeSize, 0,  halfNodeSize)
            };

            // エッジのペア
            int[][] edges = new int[][] {
                {0,1},{1,2},{2,3},{3,0}
            };

            // 線の色
            float r = 0.5f, g = 0.5f, b = 0.5f, a = 0.5f;

            for (int[] edge : edges) {
                Vector3f v0 = corners[edge[0]];
                Vector3f v1 = corners[edge[1]];
                buffer.vertex(matrix, v0.x(), v0.y(), v0.z())
                    .color(r,g,b,a)
                    .uv(0f,0f).uv(0f,0f)
                    .uv2(LightTexture.pack(15, 15))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .normal(0f,0f,0f)
                    .endVertex();
                buffer.vertex(matrix, v1.x(), v1.y(), v1.z())
                    .color(r,g,b,a)
                    .uv(0f,0f)
                    .uv2(LightTexture.pack(15, 15))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .normal(0f,0f,0f)
                    .endVertex();
            }

            poseStack.popPose();

            // ノードが子ノードなら
            /*if(WrittenBoardDrawType.isChildNode(hitBlockState.getValue(AbstractWrittenBoardBlock.CIRCLE_TYPE))){
                // pointPosをブロックエンティティの中心に
                Vec3 nodeCenter = entity.getBlockPos().getCenter();
                // 線の選択描画
                renderCircleHitBox(poseStack, bufferSource, instance, entityPos, entity, nodeCenter);
                renderLineHitBox(poseStack, bufferSource, instance, entityPos, entity, nodeCenter);
            }*/
        }
    }

    // 線分の描画
    public static void renderLine(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, int combinedLight, int combinedOverlay, float[] debugColor, Minecraft instance, BlockPos entityPos, BlockPos linePos, float angle, ResourceLocation lineResource, @Nullable ResourceLocation arrowResource) {
        // 長さ
        float lineLength = (float) java.lang.Math.sqrt(entityPos.distSqr(linePos));

        // 矢印があれば描画
        if(arrowResource!= null){
            poseStack.pushPose();
            poseStack.translate(0,1.0005f,0);
            poseStack.rotateAround(
                new Quaternionf().rotateY(angle),0.5f,0.5f,0.5f
            );
            poseStack.translate(ARROW_PADDING + 8f/16f,0,0);

            int visualLight = calcLight(getVisualPosLight(
                instance.level, entityPos.above().getCenter().add(new Vec3(Math.cos(angle),0,Math.sin(angle)).scale(ARROW_PADDING + 8f/16f))), combinedLight);

            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null,
                Minecraft.getInstance().getModelManager().getModel(arrowResource),
                debugColor[0],debugColor[1],debugColor[2], visualLight, combinedOverlay
            );

            poseStack.popPose();
            // パディングの分長さを縮める
            lineLength -= ARROW_PADDING;
        }

        // 長さから配列数を決める
        int divideCount = (int) Math.max(1, Math.floor(lineLength));
        float scale = lineLength / divideCount;

        poseStack.pushPose();
        poseStack.translate(0,1,0);
        poseStack.rotateAround(
            new Quaternionf().rotateY(angle),0.5f,0.5f,0.5f
        );
        // パディングの分ずらす
        if(arrowResource!= null){
            poseStack.translate(ARROW_PADDING,0,0);
        }
        poseStack.scale(scale,1,1);
        poseStack.translate(-0.5,0,0);

        // 線分上に描画
        for (int i = 0; i < divideCount; i++) {
            poseStack.translate(1,0,0);
            int visualLight = calcLight(getVisualPosLight(
                instance.level, entityPos.above().getCenter().lerp(linePos.above().getCenter(),(i+0.5)/divideCount)), combinedLight);

            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null,
                Minecraft.getInstance().getModelManager().getModel(lineResource),
                debugColor[0],debugColor[1],debugColor[2], visualLight, combinedOverlay
            );
        }
        poseStack.popPose();
    }

    private void renderLineHitBox(PoseStack poseStack, MultiBufferSource bufferSource, Minecraft instance, BlockPos entityPos, AbstractWrittenBoardBlockEntity entity, Vec3 pointPos) {
        List<BlockPos> linePairs = entity.getLinePairs();
        Vec3 surfacePosition = entityPos.getCenter().multiply(1,0,1);
        Vec3 surfaceLocation = pointPos.multiply(1,0,1);
        // 距離を取得
        double distance = surfacePosition.distanceTo(surfaceLocation);
        List<BlockPos> selectPairs = AbstractWrittenBoardBlockEntity.getLineFromPos(linePairs, AbstractWrittenBoardBlockEntity.CLICK_SIZE,surfacePosition,surfaceLocation,distance);

        final float halfWidth = (float) AbstractWrittenBoardBlockEntity.CLICK_SIZE;
        final float HEIGHT = 1/16f;
        final float halfHeight = HEIGHT / 2f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINES);
        for (BlockPos pairPos : selectPairs) {
            Vec3 surfacePairPos = pairPos.getCenter().multiply(1,0,1);
            // 角度を計算
            float angle = Math.atan2(entityPos.getZ() - pairPos.getZ(), pairPos.getX() - entityPos.getX());
            // 長さを計算
            float lineLength = (float) java.lang.Math.sqrt(entityPos.distSqr(pairPos));
            final float halfLength = (lineLength- ARROW_PADDING) / 2f;
            // 矩形の中心座標
            Vec3 midpoint = surfacePairPos.subtract(surfacePosition).scale(0.5);

            poseStack.pushPose();
            poseStack.translate(midpoint.x + 0.5,1.0,midpoint.z + 0.5);
            poseStack.rotateAround(
                new Quaternionf().rotateY(angle),0,0,0
            );
            // 現在の行列を取得
            Matrix4f matrix = poseStack.last().pose();

            // ローカル空間での 8 頂点（長手方向 = +X）
            Vector3f[] corners = new Vector3f[] {
                new Vector3f(-halfLength, 0, -halfWidth),
                new Vector3f( halfLength, 0, -halfWidth),
                new Vector3f( halfLength, 0,  halfWidth),
                new Vector3f(-halfLength, 0,  halfWidth)
            };

            // エッジのペア
            int[][] edges = new int[][] {
                {0,1},{1,2},{2,3},{3,0}
            };

            // 線の色
            float r = 0.5f, g = 0.5f, b = 0.5f, a = 0.5f;

            for (int[] edge : edges) {
                Vector3f v0 = corners[edge[0]];
                Vector3f v1 = corners[edge[1]];
                buffer.vertex(matrix, v0.x(), v0.y(), v0.z())
                    .color(r,g,b,a)
                    .uv(0f,0f).uv(0f,0f)
                    .uv2(LightTexture.pack(15, 15))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .normal(0f,0f,0f)
                    .endVertex();
                buffer.vertex(matrix, v1.x(), v1.y(), v1.z())
                    .color(r,g,b,a)
                    .uv(0f,0f)
                    .uv2(LightTexture.pack(15, 15))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .normal(0f,0f,0f)
                    .endVertex();
            }

            poseStack.popPose();

        }
    }

    // 同心円の描画
    public static void renderCircle(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, int combinedLight, int combinedOverlay, float[] circleColor, Minecraft instance, BlockPos entityPos, double circleRadius, ResourceLocation circleResource) {
        // 円周長
        float circumLength = (float) ((circleRadius + AbstractWrittenBoardBlockEntity.CLICK_SIZE) * 2 * Math.PI);
        // 円周の長さから配列数を決める
        int divideCount = (int)Math.ceil(circumLength / 4) * 4;
        float scale = circumLength / divideCount;
        // 円周上に描画
        for (int i = 0; i < divideCount; i++) {
            float angle = (float) (i * 2 * Math.PI/divideCount);
            poseStack.pushPose();
            poseStack.translate(0,1.0003f, circleRadius);
            poseStack.rotateAround(
                new Quaternionf().rotateY(angle), 0.5f, 0.5f, 0.5f - (float) circleRadius);
            poseStack.translate((1-scale)*0.5, 0,0);
            poseStack.scale(scale,1,1);
            int visualLight = calcLight(getVisualPosLight(
                instance.level,new Vec3(Math.sin(angle) * circleRadius,1,
                    Math.cos(angle) * circleRadius).add(entityPos.getCenter())), combinedLight);
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null,
                Minecraft.getInstance().getModelManager().getModel(circleResource),
                circleColor[0],circleColor[1],circleColor[2], visualLight, combinedOverlay
            );
            poseStack.popPose();
        }
    }

    private void renderCircleHitBox(PoseStack poseStack, MultiBufferSource bufferSource, Minecraft instance, BlockPos entityPos, AbstractWrittenBoardBlockEntity entity, Vec3 pointPos) {
        List<Double> circleRadii = entity.getCircleRadii();
        Vec3 surfacePosition = entityPos.getCenter().multiply(1,0,1);
        Vec3 surfaceLocation = pointPos.multiply(1,0,1);
        // 距離を取得
        double distance = surfacePosition.distanceTo(surfaceLocation);
        double selectRadius = AbstractWrittenBoardBlockEntity.getCircleFromPos(circleRadii, AbstractWrittenBoardBlockEntity.CLICK_SIZE, distance);
        if(selectRadius == 0){
            return;
        }
        // 円周長
        float selectCircumLength = (float) ((selectRadius + 0.125) * 2 * Math.PI);
        // 円周の長さから配列数を決める
        int divideCount = (int)Math.ceil(selectCircumLength / 4) * 4;

        double[] boundaryRadii = new double[]{
            selectRadius - AbstractWrittenBoardBlockEntity.CLICK_SIZE,
            selectRadius + AbstractWrittenBoardBlockEntity.CLICK_SIZE
        };
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINES);
        for (double circleRadius : boundaryRadii) {
            // 円周長
            float circumLength = (float) ((circleRadius + 0.125) * 2 * Math.PI);

            float scale = (circumLength / divideCount) * 0.5f;
            // 円周上に描画
            for (int i = 0; i < divideCount; i++) {
                float angle = (float) (i * 2 * Math.PI / divideCount);
                poseStack.pushPose();
                poseStack.translate(0.5f,1.0003f, circleRadius + 0.5f);
                poseStack.rotateAround(
                    new Quaternionf().rotateY(angle), 0f, 0f, 0f - (float) circleRadius);

                // 現在の行列を取得
                Matrix4f matrix = poseStack.last().pose();

                // 線の色
                float r = 0.5f, g = 0.5f, b = 0.5f, a = 0.5f;

                buffer.vertex(matrix, -scale, 0, 0)
                    .color(r,g,b,a)
                    .uv(0f,0f).uv(0f,0f)
                    .uv2(LightTexture.pack(15, 15))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .normal(0f,0f,0f)
                    .endVertex();
                buffer.vertex(matrix, scale, 0, 0)
                    .color(r,g,b,a)
                    .uv(0f,0f)
                    .uv2(LightTexture.pack(15, 15))
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .normal(0f,0f,0f)
                    .endVertex();

                poseStack.popPose();
            }
        }
    }

    //ブロックの光レベルの取得
    public static int calcLight(int combinedLight, int levelLight){
        int skyLight = combinedLight >> 20 & 15;
        int blockLight = combinedLight >> 4 & 15;
        //計算
        int maxBlockLight = Math.max(blockLight, levelLight);
        return (skyLight << 20| maxBlockLight << 4);
    }

    protected static int getVisualPosLight(@Nullable ClientLevel level, Vec3 visualPos) {
        if(level == null){
            return 0;
        }
        //Vec3 visualPos = getVisualPos();
        BlockPos blockpos = BlockPos.containing(visualPos.x,visualPos.y,visualPos.z);
        return level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
    }
}
