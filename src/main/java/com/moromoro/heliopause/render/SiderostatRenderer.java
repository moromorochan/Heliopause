package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.heliopause.EnumProperty.SiderostatTopState;
import com.moromoro.heliopause.block.SiderostatTopBlock;
import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import static com.moromoro.heliopause.blockEntity.SiderostatBlockEntity.SLICE_ANGLE;

public class SiderostatRenderer<T extends SiderostatBlockEntity> implements BlockEntityRenderer<T> {
    protected static long lastFrameTime;
    protected static double currentRotation;

    private static final double FADE_ANGLE = SLICE_ANGLE/2; // 表示をフェードイン・アウトさせる角度幅
    private final BlockRenderDispatcher blockRenderer;
    private final BakedModel springModel;
    private final BakedModel moonPhantomModel;
    public SiderostatRenderer(BlockEntityRendererProvider.Context context) {
        //super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.springModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.SIDEROSTAT_SPRING);
        this.moonPhantomModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.SIDEROSTAT_MOON);
    }

    /*private BakedModel fetchModel(){
        ModelManager manager = Minecraft.getInstance().getModelManager();
        if(moonPhantomModel != null && moonPhantomModel != manager.getMissingModel()){
            return this.moonPhantomModel;
        }
        try {
            BakedModel model = manager.getModel(new ResourceLocation(Heliopause.MODID, "phantom/moon"));
            if(model != manager.getMissingModel()){
                this.moonPhantomModel = model;
                return model;
            }
        }catch (Exception e){
            Heliopause.LOGGER.debug("Missing model moonPhantomModel");
        }
        this.moonPhantomModel = manager.getMissingModel();
        return this.moonPhantomModel;
    }*/

    @Override
    public void render(@NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //super.render(entity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        Minecraft instance = Minecraft.getInstance();
        if(instance.level!=null){
            // 描画用位置
            // 描画用角度
            float currentBowAngle = 0;
            // ブロックステートを取得して起動状態か確認
            BlockState blockState = instance.level.getBlockState(entity.getBlockPos().above());
            if(blockState.getBlock() instanceof SiderostatTopBlock) {
                if(blockState.getValue(SiderostatTopBlock.FACING_SIDEROSTAT)== SiderostatTopState.MOVING){
                    if(entity.getSynced()){
                        // 角度を更新
                        currentBowAngle = instance.level.getSunAngle(partialTicks) + (float) (Math.toRadians(90));
                        // 月の濃さを計算
                        float moonStrength = calcMoonStrength(entity, (Math.toDegrees(currentBowAngle) +180)%360, entity.getCanSeeSkies());

                        if(moonStrength > 0){
                            // 月を描画
                            poseStack.pushPose();
                            renderMoon(poseStack, bufferSource, partialTicks, moonStrength  * 0.84f, entity.getBlockPos(), combinedLight, combinedOverlay);
                            poseStack.popPose();
                        }
                    }else{
                        // ゼンマイの角度を採用
                        currentBowAngle = (float) Math.toRadians(entity.getSpringAmount() + 180 - (Math.ceil(entity.getSpringCharge() / 2.0) * (1.0 - partialTicks)));
                    }

                    // 時刻に合わせて回転
                    poseStack.pushPose();
                    renderBow(poseStack, bufferSource, currentBowAngle, combinedLight, combinedOverlay);
                    poseStack.popPose();
                }
                // モーター描画
                poseStack.pushPose();
                renderSpring(poseStack, bufferSource, currentBowAngle, combinedLight, combinedOverlay);
                poseStack.popPose();
            }
        }
    }

    private float calcMoonStrength(T entity, double angleDeg, short canSeeSkies) {
        // 今の視野を参照
        if(!entity.isAngleVisible(angleDeg, canSeeSkies)){
            return 0.0f;
        }
        // 角度が範囲の後半なら
        if(angleDeg % SLICE_ANGLE > SLICE_ANGLE - FADE_ANGLE){
            // 次の視野を参照
            if(entity.isAngleVisible(angleDeg + SLICE_ANGLE, canSeeSkies)){
                return 1.0f;
            }else{
                return (float) ((SLICE_ANGLE -(angleDeg % SLICE_ANGLE))/FADE_ANGLE);
            }
        }
        // 前半なら
        else if (angleDeg % SLICE_ANGLE < FADE_ANGLE){
            // 前の視野を参照
            if(entity.isAngleVisible(angleDeg - SLICE_ANGLE, canSeeSkies)){
                return 1.0f;
            }else{
                return (float) ((angleDeg % SLICE_ANGLE)/FADE_ANGLE);
            }
        }
        return 1.0f;
    }

    private void renderMoon(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, float MoonStrength, BlockPos blockPos, int combinedLight, int combinedOverlay) {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null){
            return;
        }
        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        if(currentFrameTime != lastFrameTime){
            double deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0F;
            if(deltaTime > 0){
                currentRotation += (float) (deltaTime/* * 10f*/);
                currentRotation %= 360 * 2 * Math.PI; // 約数の多い回転数で切る
                lastFrameTime = currentFrameTime;
                //Heliopause.LOGGER.debug(String.valueOf(deltaTime));
            }
        }

        double localRotation = currentRotation + RandomSource.create(blockPos.asLong()).nextInt(360)/(6 * Math.PI);

        // 時刻から回転角度を設定
        double rotationOffset = (localRotation / 3) % (2*Math.PI);

        double scaleOffset = (Math.cos(localRotation / 2) % (2*Math.PI)) * 0.02;

        // 時刻から上下動を設定
        double waveOffset = /*-scaleOffset * 0.5 + */(Math.cos(localRotation) / 60) % (2*Math.PI);

        //位置調整
        //poseStack.translate(0.5, (0.5 * 1.26 - 0.5) * scaleOffset + waveOffset, 0.5);
        // 中心へ移動
        poseStack.translate(0.5,1.5,0.5);
        //傾きの設定
        poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
        //回転
        poseStack.mulPose(new Quaternionf().rotateAxis((float) rotationOffset, 1, 1, 1));
        //スケール調整
        poseStack.scale((float)(1 + scaleOffset) * MoonStrength, (float)(1 + scaleOffset) * MoonStrength, (float)(1 + scaleOffset) * MoonStrength);
        // 中心から戻す
        poseStack.translate(-0.5,-0.5,-0.5);
        // 位置調整
        poseStack.translate(waveOffset,waveOffset,waveOffset);

        // ブレンド有効化
        /*RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, Math.max(0f, Math.min(1f, MoonStrength)));*/

        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.translucent()), null, moonPhantomModel,
            1,1,1, 0xF000F0, combinedOverlay);

        // ブレンド無効化
        /*RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();*/
    }

    private void renderBow(PoseStack poseStack, MultiBufferSource bufferSource, float currentMoonAngle, int combinedLight, int combinedOverlay) {
        // 描画するモデル元
        BlockState bowBlockState = BlockRegistry.SIDEROSTAT_TOP.get().defaultBlockState();
        // 角度を計算
        //float currentMoonAngle = level.getSunAngle(partialTicks) + (float) (Math.toRadians(90));

        poseStack.translate(0.5,1.5,0.5);
        poseStack.mulPose(new Quaternionf().rotateZ(currentMoonAngle));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.renderSingleBlock(bowBlockState, poseStack, bufferSource, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.cutout());
    }

    private void renderSpring(PoseStack poseStack, MultiBufferSource bufferSource, float currentMoonAngle, int combinedLight, int combinedOverlay) {
        // 描画するモデル元

        //BlockState bowBlockState = BlockRegistry.SIDEROSTAT_MOTOR.get().defaultBlockState();
        // 角度を計算
        //float currentMoonAngle = level.getSunAngle(partialTicks)*10;

        poseStack.translate(0.5,0.5,0.5);
        poseStack.mulPose(new Quaternionf().rotateZ(currentMoonAngle*10));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.solid()), null, springModel,
            1f,1f,1f, combinedLight, combinedOverlay);
        //blockRenderer.renderSingleBlock(bowBlockState, poseStack, bufferSource, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.cutout());
    }

}
