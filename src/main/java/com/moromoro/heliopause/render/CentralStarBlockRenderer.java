package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.CentralStarBlockEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.recipe.OrreryIngredient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CentralStarBlockRenderer<T extends CentralStarBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;

    public CentralStarBlockRenderer(BlockEntityRendererProvider.Context context) {
        super();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }
    protected static HashMap<BlockPos,List<CircumstellarIngredient>> ingredientsList = new HashMap<>();

    public static void updateData(BlockPos pos, List<CircumstellarIngredient> ingredientList) {
        if(ingredientList.isEmpty()){removeData(pos); return;}

        if (ingredientsList.containsKey(pos)){
            ingredientsList.replace(pos,(ingredientList));
        }else{
            ingredientsList.put(pos,ingredientList);
        }
    }
    public static void removeData(BlockPos pos){
        ingredientsList.remove(pos);
    }

    public double calcSize(double sizeMin ,double sizeMax,double fillRatio) {
        float power = 6f;
        double size = (sizeMin + (sizeMax-sizeMin) * (java.lang.Math.pow(Math.clamp(0,1,fillRatio),1f/power)));
        if(size == 0){
            Heliopause.LOGGER.error("Rendering failure on orb size calculation.");
        }
        return size;
    }

    public float calcOffsetY(float orbSize) {
        return 1f/16f;
    }

    @Override
    public void render(@NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        /*if(partialTicks<=0.1f){
            entity.requestModelDataUpdate();
        }*/
        //中心素材を取得
        OrreryIngredient centerIngredient = entity.getCenterIngredient();
        if(!centerIngredient.isEmpty()){
            // 現在のフレーム時間を取得
            long currentFrameTime = System.nanoTime();
            // デルタ時間を計算（秒単位）
            float deltaTime = (currentFrameTime - entity.getLastFrameTime()) / 1_000_000_000.0F;

            //オーブのサイズを格納
            float orbSize = centerIngredient.getFluidStack().isEmpty()? 1: (float)calcSize(0,1, (double) centerIngredient.getFluidStack().getAmount() /entity.getTankCapacity(0));
            //回転オフセットを取得
            float rotationOffset = entity.getRotationOffset();
            //上下動オフセットを取得
            float waveOffset = entity.getWaveOffset();

            //回転オフセットに加算
            entity.setRotationOffset(rotationOffset + (deltaTime / orbSize) * getRotationSpeed());
            //上下動オフセットに加算
            entity.setWaveOffset(waveOffset + (deltaTime / orbSize) * 170f);

            //オフセットを使用する形に変形
            float itemRotationOffset = (float) (-rotationOffset*(Math.PI/180));
            float itemWaveOffset = (orbSize*0.03f * Math.cos(Math.toRadians(waveOffset)));

            //座標系開始
            poseStack.pushPose();

            //中心星の描画
            switch (centerIngredient.getType()) {
                case "block" -> {
                    BlockState centerBlockState = entity.getCenterBlockState();
                    //位置調整
                    poseStack.translate(0.5, itemWaveOffset, 0.5);
                    //スケール調整
                    poseStack.scale(0.625f, 0.625f, 0.625f);
                    //傾きの設定
                    poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
                    //回転
                    poseStack.mulPose(new Quaternionf().rotateAxis(itemRotationOffset, 1, 1, 1));
                    //描画
                    blockRenderer.renderSingleBlock(centerBlockState, poseStack, bufferSource, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.translucent());
                }
                case "item" -> {
                    ItemStack centerItem = centerIngredient.getItemStack();
                    if (centerItem.getItem() instanceof BlockItem blockItem) {
                        BlockState centerBlockState = blockItem.getBlock().defaultBlockState();
                        //位置調整
                        poseStack.translate(0.5, calcOffsetY(orbSize) + itemWaveOffset, 0.5);
                        //スケール調整
                        poseStack.scale(0.8f, 0.8f, 0.8f);
                        //傾きの設定
                        poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
                        //回転
                        poseStack.mulPose(new Quaternionf().rotateAxis(itemRotationOffset, 1, 1, 1));
                        //描画
                        blockRenderer.renderSingleBlock(centerBlockState, poseStack, bufferSource, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.translucent());
                    } else {
                        //位置調整
                        poseStack.translate(0.5, calcOffsetY(orbSize) + 0.5f + itemWaveOffset, 0.5);
                        //スケール調整
                        poseStack.scale(0.8f, 0.8f, 0.8f);
                        //回転
                        poseStack.mulPose(new Quaternionf().rotateY(itemRotationOffset));
                        //描画
                        itemRenderer.renderStatic(centerItem, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, bufferSource, entity.getLevel(), 0);
                    }
                }
                case "fluid" -> {
                    FluidStack centerFluid = centerIngredient.getFluidStack();
                    //回転
                    //poseStack.mulPose(new Quaternionf().rotateY(rotationOffset));
                    //渡すデータをつくる
                    HashMap<String, Object> renderingRequires = new HashMap<>();
                    renderingRequires.put("fluidStack", centerFluid);
                    renderingRequires.put("combinedLight", combinedLight);
                    renderingRequires.put("orbSize", orbSize);
                    renderingRequires.put("rotationOffset", rotationOffset);
                    renderingRequires.put("waveOffset", waveOffset);
                    renderingRequires.put("combinedOffset", new Vec3(0f, calcOffsetY(orbSize), 0f));

                    //描画
                    AbstractFluidOrbBlockRenderer.renderFluid(poseStack, bufferSource, renderingRequires);
                }
                default -> {
                    Heliopause.LOGGER.warn("Unexpected ingredient on center. type: {}", centerIngredient.getType());
                    //throw new IllegalStateException();
                }
            }

            poseStack.popPose();
            if(Minecraft.getInstance().options.renderDebug){//惑星圏のデバッグ表示/追加UI表示
                int circleColor = FastColor.ARGB32.color(255, 255,255,255);
                //内側
                renderCircle(poseStack, bufferSource, 0.98f, circleColor);
                renderCircle(poseStack, bufferSource, 1.02f, circleColor);
                //外側
                renderCircle(poseStack, bufferSource, 4.98f, circleColor);
                renderCircle(poseStack, bufferSource, 5.02f, circleColor);
            }

            //衛星の描画
            List<CircumstellarIngredient> ingredients = ingredientsList.getOrDefault(entity.getBlockPos(), new ArrayList<>());//entity.getCircumstellars();
            //デバッグ用のカラーピッカー
            int colorIndex = 1;

            for (CircumstellarIngredient ingredient : ingredients) {
                if (!ingredient.getDisk_shaped() && shouldRenderSatellite(entity, ingredient, partialTicks)) {
                    //Heliopause.LOGGER.debug("shouldRenderSatellite passed");
                    if (ingredient/*.getFluidStack()*/.isValid()) {
                        //衛星の位置を取得
                        float orbitalRadius = ingredient.getOrbitalRadius();
                        float smoothRevolution = getSmoothRevolution(entity, ingredient, partialTicks, orbitalRadius);
                        Vec3 satPos = getSatPos(smoothRevolution, orbitalRadius);
                        //自転オフセットを公転から用意する
                        float satRot = -(/*ingredient.getRotationRatio()*/(ingredient.getSlotId() * 3 + 3) * smoothRevolution) % 360;
                        switch (ingredient.getIngredient().getType()){
                            case "fluid" ->{
                                //衛星のサイズを計算
                                float satSize = ingredient.getSatRadius(entity.getTankCapacity(0));
                                satPos = satPos.add(new Vec3(0, calcOffsetY(satSize), 0));

                                //衛星の位置から光の影響を取得
                                int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), entity.getBlockPos().getCenter().add(satPos));
                                //レンダリング
                                renderFluidSatellite(satPos, satSize * 2, satRot, ingredient.getFluidStack(), entity, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);

                            }
                            case "item" ->{
                                //衛星の位置から光の影響を取得
                                int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), entity.getBlockPos().getCenter().add(satPos));
                                //レンダリング
                                renderItemSatellite(satPos, satRot, ingredient.getItemStack(), entity, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                            }
                            default -> {
                                Heliopause.LOGGER.warn("Unexpected ingredient on circle. type: {}", centerIngredient.getType());
                            }
                        }
                        if (Minecraft.getInstance().options.renderDebug) {//有効範囲のデバッグ表示/追加UI表示
                            int circleColor = FastColor.ARGB32.color(255, (colorIndex & 0x1) != 0 ? 255 : 0, (colorIndex & 0x2) != 0 ? 255 : 0, (colorIndex & 0x4) != 0 ? 255 : 0);

                            renderCircle(poseStack, bufferSource, satPos, 0.4f, circleColor);
                            renderCircle(poseStack, bufferSource, Vec3.ZERO, ingredient.getOrbitalRadius(), circleColor);
                            colorIndex++;
                        }
                    }
                } else if (Minecraft.getInstance().options.renderDebug) {//リングの有効範囲のデバッグ表示/追加UI表示
                    float ringRadius = ingredient.getOrbitalRadius();
                    float ringWidthHalf = T.calcRingWidth(ingredient.getAmount(), ringRadius) / 2f;
                    float ringRevolution = -ingredient.getRevolutionOffset()+90;
                    float ringSpreadHalf = -(float) ingredient.getRotationRatio() / 2;
                    float innerRadius = ringRadius - ringWidthHalf;
                    float outerRadius = ringRadius + ringWidthHalf;
                    int circleColor = FastColor.ARGB32.color(255, (colorIndex & 0x1) != 0 ? 255 : 0, (colorIndex & 0x2) != 0 ? 255 : 0, (colorIndex & 0x4) != 0 ? 255 : 0);
                    renderArc(poseStack, bufferSource,ringRevolution-ringSpreadHalf,ringRevolution+ringSpreadHalf, innerRadius, circleColor);
                    //renderCircle(poseStack, bufferSource, innerRadius, circleColor);
                    renderTeeth(poseStack, bufferSource, innerRadius, circleColor, false);
                    renderArc(poseStack, bufferSource,ringRevolution-ringSpreadHalf,ringRevolution+ringSpreadHalf, outerRadius, circleColor);
                    //renderCircle(poseStack, bufferSource, outerRadius, circleColor);
                    renderTeeth(poseStack, bufferSource, outerRadius, circleColor, true);

                    colorIndex++;
                }
            }
            // 現在のフレーム時間を保存
            entity.setLastFrameTime(currentFrameTime);
        }
    }

    //衛星の位置(原点=ブロックエンティティ)を取得
    private Vec3 getSatPos(float revolutionOffset, float orbitRadius){
        //極座標を取得
        //ラジアンへ変換
        float revolutionRadians = Math.toRadians(revolutionOffset);
        //極座標から直交座標へ変換
        return new Vec3(orbitRadius * Math.sin(revolutionRadians), 0, orbitRadius * Math.cos(revolutionRadians));
    }

    private float getSmoothRevolution(T entity, CircumstellarIngredient ingredient, float partialTicks, float radius) {
        float revolution = ingredient.getRevolutionOffset();
        //前回tick時点のrevolutionを計算
        float prevRevolution = revolution - Math.toRadians(Mth.sqrt(entity.getCentForce() / radius) / radius);
        //滑らかな巡行を計算
        float smoothRevolution = Math.lerp(revolution, prevRevolution, partialTicks);
        return smoothRevolution;
    }

    //衛星がそれぞれ画面内にあるかの判定
    protected boolean shouldRenderSatellite(T entity, CircumstellarIngredient ingredient, float partialTicks){
        //形状を確認 円盤ならスキップ
        if(!ingredient.getDisk_shaped()) {
            //衛星のサイズを計算
            float satSize = ingredient.getSatRadius(entity.getTankCapacity(0));
            //衛星の位置を取得
            float orbitalRadius = ingredient.getOrbitalRadius();
            float smoothRevolution = getSmoothRevolution(entity, ingredient, partialTicks, orbitalRadius);
            Vec3 satPos = getSatPos(smoothRevolution, orbitalRadius).add(entity.getBlockPos().getCenter()).add(new Vec3(0,calcOffsetY(satSize),0));
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
    }
    //ブロック自体が画面外でも、衛星に描画が必要な場合の判定
    protected boolean shouldRenderListSatellites(T entity, float partialTicks){
        List<CircumstellarIngredient> ingredients = entity.getCircumstellars();
        for (CircumstellarIngredient ingredient : ingredients) {
            if(shouldRenderSatellite(entity, ingredient, partialTicks)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(T entity) {
        return shouldRenderListSatellites(entity, 0);
    }

    private void renderFluidSatellite(
        Vec3 satPos, float satSize, float satRot, FluidStack fluidStack,
        @NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){

        //渡すデータをつくる
        HashMap<String,Object> renderingRequires = new HashMap<>();
        renderingRequires.put("fluidStack",fluidStack);

        renderingRequires.put("combinedLight", combinedLight);

        renderingRequires.put("orbSize",satSize);
        renderingRequires.put("rotationOffset", satRot);
        renderingRequires.put("waveOffset", satRot*(int)(3f/satSize));
        renderingRequires.put("combinedOffset",satPos);

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        // 中心をブロックの中心に合わせる
        //poseStack.translate(0.5, 0.5, 0.5);
        //液体の見た目をつくるメソッドを呼び出す
        AbstractFluidOrbBlockRenderer.renderFluid(poseStack,bufferSource,renderingRequires);
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();
    }

    private void renderItemSatellite(
        Vec3 satPos, float satRot, ItemStack itemStack,
        @NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int satCombLight, int combinedOverlay){
        //オフセットを使用する形に変形
        float orbSize = 1.0f;
        float itemRotationOffset = (float) (-satRot*(Math.PI/180));
        float itemWaveOffset = (orbSize*0.03f * Math.cos(Math.toRadians(satRot*(int)(3f/orbSize))));
        poseStack.pushPose();
        poseStack.translate(satPos.x,satPos.y,satPos.z);
        //フルブロックアイテムの場合
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState centerBlockState = blockItem.getBlock().defaultBlockState();
            if(centerBlockState.getCollisionShape(null,null).equals(Shapes.block())){
                //位置調整
                poseStack.translate(0.5, 0.25f + itemWaveOffset, 0.5);
                //スケール調整
                poseStack.scale(0.25f, 0.25f, 0.25f);
                //傾きの設定
                poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
                //回転
                poseStack.mulPose(new Quaternionf().rotateAxis(itemRotationOffset, 1, 1, 1));
                //描画
                blockRenderer.renderSingleBlock(centerBlockState, poseStack, bufferSource, satCombLight, combinedOverlay, ModelData.EMPTY, RenderType.translucent());
                poseStack.popPose();
                return;
            }
        }

        //位置調整
        poseStack.translate(0.5, calcOffsetY(orbSize) + 0.5f + itemWaveOffset, 0.5);
        //スケール調整
        poseStack.scale(0.5f, 0.5f, 0.5f);
        //回転
        poseStack.mulPose(new Quaternionf().rotateY(itemRotationOffset));
        //描画
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, satCombLight, combinedOverlay, poseStack, bufferSource, entity.getLevel(), 0);

        poseStack.popPose();
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
            float angle = Math.toRadians(angleStart) + i * angleIncrement;
            //頂点座標を用意
            float x1 = circleRadius * (float) Math.cos(angle);
            float y1 = circleRadius * (float) Math.sin(angle);

            buffer.vertex(poseStack.last().pose(), x1, 0, y1)
                .color(red, green, blue, 255)
                .normal(0,0,0)
                .endVertex();
        }
        //終了
        poseStack.popPose();
    }

    private void renderArc(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,float angleStart, float angleEnd, float circleRadius, int circleColor){
        renderArc(poseStack, bufferSource, new Vec3(0,calcOffsetY(1), 0),angleStart, angleEnd, circleRadius, circleColor);
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
            float x1 = circleRadius * (float) Math.cos(angle);
            float y1 = circleRadius * (float) Math.sin(angle);
            float x2 = (circleRadius+ (inner ? -0.1f: 0.1f)) * (float) Math.cos(angle);
            float y2 = (circleRadius+ (inner ? -0.1f: 0.1f)) * (float) Math.sin(angle);

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
