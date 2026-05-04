package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.heliopause.block.OrreryCircleBoardBlock;
import com.moromoro.heliopause.blockEntity.OrreryCircleBoardBlockEntity;
import com.moromoro.heliopause.generic.PolarCoordinates;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.recipe.ImitationCoreAssemblyRecipe;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2d;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OrreryCircleBoardRenderer extends WrittenBoardRenderer<OrreryCircleBoardBlockEntity> {
    protected static long lastFrameTime;
    protected static double currentRotation;

    public static double FIXED_OFFSET_Y = 1.0;

    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;

    private final BakedModel itemSphereModel;
    private final BakedModel concealedSphereModel;
    private final BakedModel subSphereModel;
    private final BakedModel orbitModel;
    private final BakedModel orbitEndModel;

    private final BakedModel rockyModel;
    private final BakedModel gasStripesModel;
    private final BakedModel gasPlainModel;
    private final BakedModel fixedStarModel;
    private final BakedModel collapsarModel;

    public OrreryCircleBoardRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();

        this.itemSphereModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_ITEM_SPHERE);
        this.concealedSphereModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_CONCEALED_SPHERE);
        this.subSphereModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_SUB_SPHERE);
        this.orbitModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_ORBIT);
        this.orbitEndModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_ORBIT_END);

        this.rockyModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_ROCKY);
        this.gasStripesModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_GAS_STRIPES);
        this.gasPlainModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_GAS_PLAIN);
        this.fixedStarModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_FIXED_STAR);
        this.collapsarModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_COLLAPSAR);
    }

    @Override
    public void render(@NotNull OrreryCircleBoardBlockEntity entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        super.render(entity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        Minecraft instance = Minecraft.getInstance();
        if(instance.level == null){return;}
        BlockPos entityPos = entity.getBlockPos();

        // 材料の描画
        if(Minecraft.getInstance().options.renderDebug){//惑星圏のデバッグ表示/追加UI表示
            int circleColor = FastColor.ARGB32.color(255, 255,255,255);
            //内側
            renderCircle(poseStack, bufferSource, OrreryCircleBoardBlock.InnerLimitRadius - 0.02f, circleColor);
            renderCircle(poseStack, bufferSource, OrreryCircleBoardBlock.InnerLimitRadius + 0.02f, circleColor);
            //外側
            float outerCircleSize = (float) Math.max(0,entity.getMaxCircleRadius() - OrreryCircleBoardBlockEntity.CLICK_SIZE);//!entity.getCircleRadii().isEmpty()?(float) (Collections.max(entity.getCircleRadii()) - OrreryCircleBoardBlockEntity.CLICK_SIZE) : 0f;
            renderCircle(poseStack, bufferSource, outerCircleSize - 0.02f, circleColor);
            renderCircle(poseStack, bufferSource, outerCircleSize + 0.02f, circleColor);
        }

        // 終了タイマー
        float finishingProgress = entity.isCraftingInFinish() ? (entity.getProgressTimer() + partialTicks) / OrreryCircleBoardBlockEntity.FINISHING_TICK : 0f;
        final float finishTime = 0.05f;

        // アイテム縮小タイマー
        float itemShrinkProgress = remapProgress(0f, 0.05f, finishingProgress);

        // 工程ごとの高さオフセット
        double progressOffsetY = getProgressOffsetY(entity, partialTicks);

        //衛星の描画
        List<CircumstellarIngredient> ingredients = entity.getCircumstellars();//ingredientsList.getOrDefault(entity.getBlockPos(), new ArrayList<>());//entity.getCircumstellars();
        //デバッグ用のカラーピッカー
        int colorIndex = 1;
        for (int i = 0; i < ingredients.size(); i++) {
            CircumstellarIngredient ingredient = ingredients.get(i);
            if (!ingredient.isDiskShaped() /*&& shouldRenderSatellite(entity, ingredient, partialTicks)*/) {
                if (ingredient.isValid()) {
                    //衛星のサイズを取得
                    float satSize = CircumstellarIngredient.getSatRadius(ingredient.getStellarStack()) * (1-itemShrinkProgress);
                    //衛星の位置を取得
                    float orbitalRadius = ingredient.getOrbitalRadius();
                    float smoothRevolution =
                        (float) PolarCoordinates.revolutionProcess(orbitalRadius, ingredient.getRevolutionOffset(), (float) entity.getCentForce(partialTicks), partialTicks);//getSmoothRevolution(entity, ingredient, partialTicks, orbitalRadius);
                    Vec3 satPos = getSatPos(smoothRevolution, orbitalRadius, progressOffsetY + calcOffsetY(satSize));

                    // レシピ準備中は衛星を描画
                    if(finishingProgress < finishTime) {
                        //自転オフセットを用意する
                        float satRot = (float) getRotationOffset(entityPos.asLong() + i, 2) + smoothRevolution * 3;//* (int)Math.ceil(entity.getCentForce(partialTicks) * 30);//((int)(ingredient.getOrbitalRadius() * 3) * smoothRevolution);
                        if (ingredient.isItem()) {
                            //衛星の位置から光の影響を取得
                            int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), entity.getBlockPos().getCenter().add(satPos));
                            // 装飾で覆う
                            StellarIngredientBlockRenderer.renderItemSphere(blockRenderer, itemSphereModel, satPos, satSize, satRot, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                            //レンダリング
                            StellarIngredientBlockRenderer.renderItemSatellite(blockRenderer, itemRenderer, satPos, satSize, satRot, ingredient.getItemStack(), entity.getLevel(), entityPos, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                        } else {
                            //satPos = satPos.add(new Vec3(0, calcOffsetY(satSize), 0));

                            //衛星の位置から光の影響を取得
                            int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), entity.getBlockPos().getCenter().add(satPos));
                            // 装飾で覆う
                            StellarIngredientBlockRenderer.renderItemSphere(blockRenderer, itemSphereModel, satPos, satSize, satRot, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                            //レンダリング
                            StellarIngredientBlockRenderer.renderFluidSatellite(satPos, satSize, satRot, ingredient.getFluidStack(), partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                        }
                        // 軌道リング
                        renderOrbit(orbitalRadius, smoothRevolution, progressOffsetY, instance, entityPos, poseStack, bufferSource, combinedLight, combinedOverlay);
                    }
                    //有効範囲のデバッグ表示/追加UI表示
                    if (Minecraft.getInstance().options.renderDebug) {
                        int circleColor = FastColor.ARGB32.color(255, (colorIndex & 0x1) != 0 ? 255 : 0, (colorIndex & 0x2) != 0 ? 255 : 0, (colorIndex & 0x4) != 0 ? 255 : 0);

                        renderCircle(poseStack, bufferSource, satPos, 0.4f, circleColor);
                        renderCircle(poseStack, bufferSource, ingredient.getOrbitalRadius(), circleColor);
                    }
                }
            } else if (Minecraft.getInstance().options.renderDebug) {//リングの有効範囲のデバッグ表示/追加UI表示
                float ringRadius = ingredient.getOrbitalRadius();
                float ringWidthHalf = OrreryCircleBoardBlock.calcRingWidth(ingredient.getAmount(), ringRadius) / 2f;
                float ringRevolution = -ingredient.getRevolutionOffset()+90;
                float ringSpreadHalf = -(float) /*ingredient.getRotationRatio()*/5 / 2;
                float innerRadius = ringRadius - ringWidthHalf;
                float outerRadius = ringRadius + ringWidthHalf;
                int circleColor = FastColor.ARGB32.color(255, (colorIndex & 0x1) != 0 ? 255 : 0, (colorIndex & 0x2) != 0 ? 255 : 0, (colorIndex & 0x4) != 0 ? 255 : 0);
                renderArc(poseStack, bufferSource,ringRevolution-ringSpreadHalf,ringRevolution+ringSpreadHalf, innerRadius, circleColor);
                //renderCircle(poseStack, bufferSource, innerRadius, circleColor);
                renderTeeth(poseStack, bufferSource, innerRadius, circleColor, false);
                renderArc(poseStack, bufferSource,ringRevolution-ringSpreadHalf,ringRevolution+ringSpreadHalf, outerRadius, circleColor);
                //renderCircle(poseStack, bufferSource, outerRadius, circleColor);
                renderTeeth(poseStack, bufferSource, outerRadius, circleColor, true);
            }

            colorIndex++;
        }

        // 完了時は軌道その他を描画
        if(finishingProgress >= finishTime && !ingredients.isEmpty()) {
            // 総数を取得
            int ingredientCount = ingredients.size();
            // 軌道整列タイマー
            //float orbitAlignProgress = remapProgress(0.05f, 0.25f, finishingProgress);
            // 軌道縮小タイマー
            float orbitShrinkProgress = remapProgress(0.9f, 1.0f, finishingProgress);
            // 結果拡大タイマー
            float resultSphereDilateProgress = remapProgress(0.05f, 0.1f, finishingProgress);
            float resultSubSphereDilateProgress = remapProgress(0.1f, 0.15f, finishingProgress);
            // いちばん外側の角度を取得
            CircumstellarIngredient outerIngredient = ingredients.get(ingredientCount - 1);
            float outerOrbitalRadius = outerIngredient.getOrbitalRadius();
            float smoothRevolution =
                (float) PolarCoordinates.revolutionProcess(outerOrbitalRadius, outerIngredient.getRevolutionOffset(), (float) entity.getCentForce(partialTicks), partialTicks);//getSmoothRevolution(entity, ingredient, partialTicks, orbitalRadius);
            float satSize = 0.25f * resultSphereDilateProgress;
            float subSatSize = 0.25f * resultSubSphereDilateProgress;
            //Vec3 satPos = getSatPos(smoothRevolution, orbitalRadius, progressOffsetY);
            for (int i = 0; i < ingredients.size(); i++) {
                CircumstellarIngredient ingredient = ingredients.get(i);
                if (!ingredient.isDiskShaped() /*&& shouldRenderSatellite(entity, ingredient, partialTicks)*/) {
                    if (ingredient.isValid()) {
                        //衛星の位置を取得
                        float orbitalRadius = ingredient.getOrbitalRadius();
                        float orbitalVisualRad;
                        // 工程オフセット
                        if(finishingProgress > 0.9f){
                            orbitalVisualRad = (float) (orbitalRadius * sigmoidInterpolate(1 - orbitShrinkProgress, 20, 0.5, 1.0));
                        }else{
                            orbitalVisualRad = orbitalRadius;
                        }

                        // 外の角度基準で目標角度を作る
                        float targetRevolution = (float) (smoothRevolution + (2*Math.PI/ingredientCount)*i);
                        float localRevolution =
                            (float) PolarCoordinates.revolutionProcess(orbitalRadius, ingredient.getRevolutionOffset(), (float) entity.getCentForce(partialTicks), partialTicks);

                        //目標角度をローカル角度の先へ
                        while (targetRevolution < localRevolution){
                            targetRevolution += (float) (2*Math.PI);
                        }
                        while (targetRevolution - (2*Math.PI) >= localRevolution){
                            targetRevolution -= (float) (2*Math.PI);
                        }

                        float interpolatedRevolution = targetRevolution;//Math.lerp(localRevolution, targetRevolution, orbitAlignProgress);
                        // 子球を描画
                        if(finishingProgress > 0.1f) {
                            for (int j = 1; j < i + 2; j++) {
                                // 子球の角度を作る
                                float subRevolution = (float) (interpolatedRevolution + (2*Math.PI/(i+2))*j);
                                Vec3 subSphereSatPos = getSatPos(subRevolution, orbitalVisualRad, progressOffsetY);
                                int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), entity.getBlockPos().getCenter().add(subSphereSatPos));
                                StellarIngredientBlockRenderer.renderItemSphere(blockRenderer, subSphereModel, subSphereSatPos, subSatSize, subRevolution, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                            }
                        }
                        // 球を描画
                        Vec3 satPos = getSatPos(interpolatedRevolution, orbitalVisualRad, progressOffsetY);
                        int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), entity.getBlockPos().getCenter().add(satPos));
                        StellarIngredientBlockRenderer.renderItemSphere(blockRenderer, concealedSphereModel, satPos, satSize, interpolatedRevolution, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                        // 軌道を描画
                        renderFinishOrbit(orbitalVisualRad, interpolatedRevolution, progressOffsetY, instance, entityPos, poseStack, bufferSource, combinedLight, combinedOverlay);
                        //renderFinishArrow();
                    }
                }
            }
        }

        // 中心星の描画
        if(entity.getProcessingRecipe() != null && (entity.isCraftingInProgress() || entity.isCraftingInFinish())) {
            Optional<? extends Recipe<?>> starOptional = instance.level.getRecipeManager().byKey(entity.getProcessingRecipe()[0]);
            if(starOptional.isPresent() && starOptional.get() instanceof ImitationCoreAssemblyRecipe star){
                final ImitationCoreAssemblyRecipe.ImitationCoreProperty coreProperty = star.getCoreProperty();
                BakedModel starModel = switch (coreProperty.model()) {
                    // 岩石
                    case "rocky" -> rockyModel;
                    // ガス縞模様
                    case "gas_stripes" -> gasStripesModel;
                    // ガス縞無し
                    case "gas_plain" -> gasPlainModel;
                    // 主系列星
                    case "fixed_star" -> fixedStarModel;
                    // 崩壊星
                    case "collapsar" -> collapsarModel;
                    default -> gasPlainModel;
                };
                float satRot = (float) getRotationOffset(entityPos.asLong(), 1);
                float satSize = (float) getCenterStarScaleOffset(coreProperty.scale(), entity, partialTicks);//coreProperty.scale() * (float) (progressOffsetY - FIXED_OFFSET_Y);
                int[] color = coreProperty.color();

                float itemWaveOffset = ((1 + satSize)*0.005f * Math.cos(satRot*(int)(1.0f/satSize)));
                poseStack.pushPose();
                //位置調整
                poseStack.translate(0.5, 0.5 + getCenterStarOffsetY(entity, partialTicks) + calcOffsetY(satSize * 0.4f), 0.5);
                //傾きの設定
                poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
                //回転
                poseStack.mulPose(new Quaternionf().rotateAxis(-satRot, 1, 1, 1));
                //スケール調整
                //float scale = 0.25f;
                poseStack.scale(satSize, satSize, satSize);
                // 中心から戻す
                poseStack.translate(-0.5,-0.5,-0.5);
                double wholeOffset = /*calcOffsetY(satSize * 0.4f) + */itemWaveOffset;// * Math.sqrt(3)/scale;//scale;
                poseStack.translate(wholeOffset,wholeOffset,wholeOffset);
                //描画
                //blockRenderer.renderSingleBlock(itemBlockState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, combinedOverlay, ModelData.EMPTY, RenderType.translucent());
                blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.translucent()), null, starModel,
                    color[0]/255f,color[1]/255f,color[2]/255f, 0xF000F0, combinedOverlay);
                poseStack.popPose();
            }
        }
    }

    private float remapProgress(float start, float end, float progress) {
        return Math.min(1, Math.max(0, progress - start) / (end - start));
    }

    private double getRotationOffset(long random, int divider) {
        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        if(currentFrameTime != lastFrameTime){
            double deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0F;
            if(deltaTime > 0){
                currentRotation += (float) (deltaTime);
                currentRotation %= 3600 * 2 * java.lang.Math.PI; // 約数の多い回転数で切る
                lastFrameTime = currentFrameTime;
            }
        }

        double localRotation = currentRotation + RandomSource.create(random).nextInt(360)/(2 * java.lang.Math.PI);

        // 時刻から回転角度を設定
        return (localRotation / divider) % (2 * java.lang.Math.PI);
    }

    public static double sigmoidInterpolate(double partialValue, double scale, double sigmoidStart, double sigmoidEnd) {
        // 範囲を反映
        double localPartialValue = Math.lerp(sigmoidStart, sigmoidEnd, partialValue);
        // 端のサイズを取得
        double minSize = 1.0 / (1.0 + Math.exp((0.5 - sigmoidStart) * scale));
        double maxSize = 1.0 / (1.0 + Math.exp((0.5 - sigmoidEnd) * scale));
        // シグモイド関数
        double localSigmoid = 1.0 / (1.0 + Math.exp((0.5 - localPartialValue) * scale));
        // 範囲を0~1にした補間を返す
        return (localSigmoid - minSize) / (maxSize - minSize);
    }

    // 工程ごとの高さオフセットを取得
    public static double getProgressOffsetY(@NotNull OrreryCircleBoardBlockEntity entity, float partialTicks) {
        double offset;
        float timer = entity.getProgressTimer() + partialTicks;

        if(entity.isCraftingInProgress()){
            //double progress = Math.min(startTick, startTick+timer) / startTick;
            float startProgress = 1 + Math.min(0,timer / OrreryCircleBoardBlockEntity.STARTING_TICK);
            offset = sigmoidInterpolate(startProgress, 10, 0, 1);
        } else if (entity.isCraftingInFinish()) {
            //double progress = timer / finishTick;
            float finishProgress = timer / OrreryCircleBoardBlockEntity.FINISHING_TICK;
            offset = 1;// + finishProgress * 0.3;
        }else {
            offset = 0.0d;
        }
        return offset + FIXED_OFFSET_Y;
    }

    private double getCenterStarOffsetY(@NotNull OrreryCircleBoardBlockEntity entity, float partialTicks){
        double offset;
        float timer = entity.getProgressTimer() + partialTicks;

        if(entity.isCraftingInProgress()){
            float startProgress = 1 + Math.min(0,timer / OrreryCircleBoardBlockEntity.STARTING_TICK);
            offset = sigmoidInterpolate( startProgress, 10, 0.4,1);
        } else if (entity.isCraftingInFinish()) {
            float finishProgress = timer / OrreryCircleBoardBlockEntity.FINISHING_TICK;
            //double start = 0.4;
            offset = 1;// + sigmoidInterpolate(progress, 10)* 0.55;
        }else {
            offset = 0.0d;
        }

        return offset + FIXED_OFFSET_Y;
    }

    private double getCenterStarScaleOffset(double baseScale, @NotNull OrreryCircleBoardBlockEntity entity, float partialTicks){
        double offset;
        float timer = entity.getProgressTimer() + partialTicks;
        //final int startTick = OrreryCircleBoardBlockEntity.STARTING_TICK;
        //final int finishTick = OrreryCircleBoardBlockEntity.FINISHING_TICK;
        double startSize = 0.2 / baseScale;
        if(entity.isCraftingInProgress()){
            float startProgress = 1 + Math.min(0,timer / OrreryCircleBoardBlockEntity.STARTING_TICK);
            //double start = 0.0;
            offset = startSize + sigmoidInterpolate( startProgress, 10, 0, 1) * (1-startSize);
        } else if (entity.isCraftingInFinish()) {
            //double progress = timer / finishTick;
            float finishProgress = timer / OrreryCircleBoardBlockEntity.FINISHING_TICK;
            if(finishProgress < 0.85){
                //double end = 0.90;
                double partProgress = remapProgress(0, 0.85f, finishProgress);//finishProgress / end;
                offset = 1 + sigmoidInterpolate(partProgress, 4, 0, 0.5) * 0.5;
            }else if (finishProgress < 0.90){
                offset = 1.5;
            }else{
                //double start = 0.95;
                double partProgress = remapProgress(0.9f,0.95f,finishProgress);//(finishProgress - start) / (1-start);
                offset = 1.5 + sigmoidInterpolate(partProgress, 20, 0.5, 1) * -1.5/*(1.5 - startSize)*/;
            }
        }else {
            offset = 0.0d;
        }

        return baseScale * offset;
    }

    /*private double getProgressCentForce(@NotNull OrreryCircleBoardBlockEntity entity, float partialTicks){
        double defaultCentForce = OrreryCircleBoardBlockEntity.DEFAULT_CENT_FORCE;
        double timer = entity.getProgressTimer() + partialTicks;
        Level level = entity.getLevel();
        if(level == null){
            return defaultCentForce;
        }
        ResourceLocation[] processingRecipe = entity.getProcessingRecipe();
        if(processingRecipe!=null && (entity.isCraftingInProgress() || entity.isCraftingInFinish())){
            Optional<? extends Recipe<?>> starOptional = level.getRecipeManager().byKey(processingRecipe[0]);
            if(starOptional.isPresent()&& starOptional.get() instanceof ImitationCoreAssemblyRecipe star){
                double starCentForce = star.getCoreProperty().centForce();
                if(entity.isCraftingInProgress()){
                    return Math.lerp(starCentForce, defaultCentForce, Math.min(0, -timer) / STARTING_TICK);
                } else {
                    return starCentForce;
                }
            }
        }
        return defaultCentForce;
    }*/

    //衛星の位置(原点=ブロックエンティティ)を取得
    private Vec3 getSatPos(float revolutionOffset, float orbitRadius, double offsetY){
        Vector2d satCoordinates = PolarCoordinates.getCartesianCoordinates(orbitRadius, revolutionOffset);
        return new Vec3(satCoordinates.x(), offsetY, satCoordinates.y());
    }

    /*private float getSmoothRevolution(OrreryCircleBoardBlockEntity entity, CircumstellarIngredient ingredient, float partialTicks, float radius) {
        float revolution = ingredient.getRevolutionOffset();
        //前回tick時点のrevolutionを計算
        float prevRevolution = revolution - Mth.sqrt((float) (getProgressCentForce(entity, partialTicks) *0.01f / radius)) / radius;
        //滑らかな巡行を計算
        return Math.lerp(revolution, prevRevolution, partialTicks);
    }*/

    //衛星がそれぞれ画面内にあるかの判定
    /*protected boolean shouldRenderSatellite(OrreryCircleBoardBlockEntity entity, CircumstellarIngredient ingredient, float partialTicks){
        //形状を確認 円盤ならスキップ
        if(!ingredient.isDiskShaped()) {
            //衛星のサイズを計算
            float satSize = CircumstellarIngredient.getSatRadius(ingredient.getStellarStack());
            //衛星の位置を取得
            float orbitalRadius = ingredient.getOrbitalRadius();
            float smoothRevolution =
                (float) PolarCoordinates.revolutionProcess(orbitalRadius, ingredient.getRevolutionOffset(), partialTicks, orbitalRadius);
            Vec3 satPos = getSatPos(smoothRevolution, (float) (orbitalRadius + Math.PI * 0.5), getProgressOffsetY(entity, partialTicks)).add(entity.getBlockPos().getCenter()).add(new Vec3(0,calcOffsetY(satSize),0));
            //値から衛星のバウンディングボックスを決定
            AABB ingredientBoundingBox = new AABB(satPos, satPos).inflate(satSize);
            // バウンディングボックスが画面内にあるかどうかを判定
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;
            Camera camera = renderer.getMainCamera();
            Matrix4f viewMatrix = new Matrix4f();
            viewMatrix.lookAt(camera.getPosition().toVector3f(),camera.getLookVector(),camera.getUpVector());
            Frustum frustum = new Frustum(viewMatrix, renderer.getProjectionMatrix(Minecraft.getInstance().options.fov().get()));
            frustum.prepare(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
            return frustum.isVisible(ingredientBoundingBox);
        }
        return false;
    }*/

    private double calcOffsetY(float satSize) {
        return StellarIngredientBlockRenderer.calcOffsetY(satSize);
    }

    //ブロック自体が画面外でも、衛星に描画が必要な場合の判定
    /*protected boolean shouldRenderListSatellites(OrreryCircleBoardBlockEntity entity, float partialTicks){
        List<CircumstellarIngredient> ingredients = entity.getCircumstellars();
        for (CircumstellarIngredient ingredient : ingredients) {
            if(shouldRenderSatellite(entity, ingredient, partialTicks)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull OrreryCircleBoardBlockEntity entity) {
        return shouldRenderListSatellites(entity, 0);
    }*/

    private void renderOrbit(float orbitalRadius, float smoothRevolution, double offsetY, Minecraft instance, BlockPos entityPos, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // 円周長
        float circumLength = (float) (orbitalRadius * 2 * Math.PI);
        // 円周の長さから配列数を決める
        int divideCount = (int)Math.ceil(circumLength / 4) * 4;
        float scale = (float) (2 * orbitalRadius * Math.tan(Math.PI / divideCount));

        // 始端を描画
        {
            double angle = (1 * 2d/divideCount + 0.5) * Math.PI - smoothRevolution;
            poseStack.pushPose();
            poseStack.translate(0,offsetY, orbitalRadius);
            poseStack.rotateAround(
                new Quaternionf().rotateY((float) angle), 0.5f, 0.5f, 0.5f - orbitalRadius);
            poseStack.translate((scale+1)*0.5, 0,0);
            poseStack.scale(-scale,1,1);
            int visualLight = calcLight(getVisualPosLight(
                instance.level,new Vec3(Math.sin(angle) * orbitalRadius,1,
                    Math.cos(angle) * orbitalRadius).add(entityPos.getCenter())), combinedLight);
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, orbitEndModel,
                1f,1f,1f, visualLight, combinedOverlay
            );
            poseStack.popPose();
        }
        // 終端を描画
        {
            double angle = ((divideCount - 1) * 2d/divideCount + 0.5) * Math.PI - smoothRevolution;
            poseStack.pushPose();
            poseStack.translate(0,offsetY, orbitalRadius);
            poseStack.rotateAround(
                new Quaternionf().rotateY((float) angle), 0.5f, 0.5f, 0.5f - orbitalRadius);
            poseStack.translate((1-scale)*0.5, 0,0);
            poseStack.scale(scale,1,1);
            int visualLight = calcLight(getVisualPosLight(
                instance.level,new Vec3(Math.sin(angle) * orbitalRadius,1,
                    Math.cos(angle) * orbitalRadius).add(entityPos.getCenter())), combinedLight);
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, orbitEndModel,
                1f,1f,1f, visualLight, combinedOverlay
            );
            poseStack.popPose();
        }
        // 円周を描画
        for (int i = 2; i < divideCount - 1; i++) {
            double angle = (i * 2d/divideCount + 0.5) * Math.PI - smoothRevolution;
            poseStack.pushPose();
            poseStack.translate(0,offsetY, orbitalRadius);
            poseStack.rotateAround(
                new Quaternionf().rotateY((float) angle), 0.5f, 0.5f, 0.5f - orbitalRadius);
            poseStack.translate((1-scale)*0.5, 0,0);
            poseStack.scale(scale,1,1);
            int visualLight = calcLight(getVisualPosLight(
                instance.level,new Vec3(Math.sin(angle) * orbitalRadius,1,
                    Math.cos(angle) * orbitalRadius).add(entityPos.getCenter())), combinedLight);
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, orbitModel,
                1f,1f,1f, visualLight, combinedOverlay
            );
            poseStack.popPose();
        }
    }

    private void renderFinishOrbit(float orbitalRadius, float smoothRevolution, double offsetY, Minecraft instance, BlockPos entityPos, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        // 円周長
        float circumLength = (float) (orbitalRadius * 2 * Math.PI);
        // 円周の長さから配列数を決める
        int divideCount = (int)Math.ceil(circumLength / 4) * 4;
        float scale = (float) (2 * orbitalRadius * Math.tan(Math.PI / divideCount));

        // 円周を描画
        for (int i = 0; i < divideCount; i++) {
            double angle = (i * 2d/divideCount + 0.5) * Math.PI - smoothRevolution;
            poseStack.pushPose();
            poseStack.translate(0,offsetY, orbitalRadius);
            poseStack.rotateAround(
                new Quaternionf().rotateY((float) angle), 0.5f, 0.5f, 0.5f - orbitalRadius);
            poseStack.translate((1-scale)*0.5, 0,0);
            poseStack.scale(scale,1,1);
            int visualLight = calcLight(getVisualPosLight(
                instance.level,new Vec3(Math.sin(angle) * orbitalRadius,1,
                    Math.cos(angle) * orbitalRadius).add(entityPos.getCenter())), combinedLight);
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, orbitModel,
                1f,1f,1f, visualLight, combinedOverlay
            );
            poseStack.popPose();
        }
    }

    private void renderArc(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, Vec3 circlePos,float angleStart, float angleEnd, float circleRadius, int circleColor){

        int red = FastColor.ARGB32.red(circleColor);
        int green = FastColor.ARGB32.green(circleColor);
        int blue = FastColor.ARGB32.blue(circleColor);

        //angleStart %=360;
        //angleEnd %= 360;
        //多角形の線分の数
        float segments = 2 + Math.round(80 * Math.abs(angleEnd-angleStart)/360);
        float angleIncrement = Math.toRadians((angleEnd-angleStart) / segments);
        //カメラの向きを取得
        //Vector3f lookVec = Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector().normalize();
        //座標系開始
        poseStack.pushPose();
        // 中心をブロックの中心に合わせる
        poseStack.translate(0.5+circlePos.x,0.5 + circlePos.y,0.5+circlePos.z);
        //描画形式を設定
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINE_STRIP);
        //円の描画
        for (int i = 0; i <= segments; i++) {
            //float angle = Math.toRadians(angleStart) + i * angleIncrement;
            //頂点座標を用意
            //float x1 = circleRadius * Math.cos(angle);
            //float y1 = circleRadius * Math.sin(angle);
            // 角度を変える
            poseStack.mulPose(new Quaternionf().rotateY(angleIncrement));
            // 円周上に合わせる
            poseStack.translate(circleRadius, 0, 0);
            
            buffer.vertex(poseStack.last().pose(), 0, 0, 0)
                .color(red, green, blue, 255)
                .normal(0,0,0)
                .endVertex();
            
            // 中心に戻す
            poseStack.translate(-circleRadius, 0, 0);
        }
        //終了
        poseStack.popPose();
    }

    private void renderArc(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,float angleStart, float angleEnd, float circleRadius, int circleColor){
        renderArc(poseStack, bufferSource, new Vec3(0,calcOffsetY(1) + FIXED_OFFSET_Y, 0),angleStart, angleEnd, circleRadius, circleColor);
    }

    private void renderCircle(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, Vec3 circlePos, float circleRadius, int circleColor){
        renderArc(poseStack, bufferSource, circlePos, 0, 360, circleRadius, circleColor);
    }
    private void renderCircle(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, float circleRadius, int circleColor){
        renderArc(poseStack,bufferSource,0,360,circleRadius,circleColor);
    }

    private void renderTeeth(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, Vec3 circlePos, float circleRadius, int circleColor, boolean inner){
        int red = FastColor.ARGB32.red(circleColor);
        int green = FastColor.ARGB32.green(circleColor);
        int blue = FastColor.ARGB32.blue(circleColor);
        //歯の数
        float segments = 32;
        float angleIncrement = (float) (2 * Math.PI / segments);

        //座標系開始
        poseStack.pushPose();
        // 中心をブロックの中心に合わせる
        poseStack.translate(0.5+circlePos.x,0.5 + circlePos.y,0.5+circlePos.z);
        //描画形式を設定
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINES);
        //円の描画
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleIncrement;
            //頂点座標を用意
            float x1 = circleRadius * Math.cos(angle);
            float y1 = circleRadius * Math.sin(angle);
            float x2 = (circleRadius+ (inner ? -0.1f: 0.1f)) * Math.cos(angle);
            float y2 = (circleRadius+ (inner ? -0.1f: 0.1f)) * Math.sin(angle);

            buffer.vertex(poseStack.last().pose(), x1, 0, y1)
                .color(red, green, blue, 255)
                .normal(0,0,0)
                .endVertex();
            buffer.vertex(poseStack.last().pose(), x2, 0, y2)
                .color(red, green, blue, 255)
                .normal(0,0,0)
                .endVertex();
        }
        //終了
        poseStack.popPose();
    }

    private void renderTeeth(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, float circleRadius, int circleColor, boolean inner){
        renderTeeth(poseStack,bufferSource,new Vec3(0,calcOffsetY(1),0),circleRadius,circleColor, inner);
    }

    //任意の位置の明るさを取得
    private int getCombinedLight(ClientLevel level, Vec3 pos){
        BlockPos blockpos = BlockPos.containing(pos.x, pos.y, pos.z);
        return level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
    }

    public float getRotationSpeed() {
        return -40f;
    }
}
