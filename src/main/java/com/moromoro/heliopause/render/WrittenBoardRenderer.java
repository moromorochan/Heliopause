package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Quaternionf;

public class WrittenBoardRenderer<T extends WrittenBoardBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

        private static final ResourceLocation DEFAULT_CIRCLE = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_default");
        //private final BakedModel defaultCircleModel;
        private static final ResourceLocation DEFAULT_LINE = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_default");
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

        // 同心円の描画
        for (double circleRadius : entity.getCircleRadii()) {
            renderCircle(poseStack, bufferSource, combinedLight, combinedOverlay, instance, entityPos, circleRadius);
        }

        // 線分の描画
        for (BlockPos linePos : entity.getLinePairs()) {
            // 片側だけ描画に使用する
            float angle = Math.atan2(entityPos.getZ() - linePos.getZ(), linePos.getX() - entityPos.getX());
            if(angle <= 0){
               continue;
            }
            renderLine(poseStack, bufferSource, combinedLight, combinedOverlay, instance, entityPos, linePos, angle);
        }
    }

    // 線分の描画
    private void renderLine(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, Minecraft instance, BlockPos entityPos, BlockPos linePos, float angle) {
        // 長さ
        float lineLength = (float) java.lang.Math.sqrt(entityPos.distSqr(linePos));
        // 長さから配列数を決める
        int divideCount = (int) Math.floor(lineLength);
        float scale = lineLength / divideCount;

        poseStack.pushPose();
        poseStack.translate(0,1,0);
        poseStack.rotateAround(
            new Quaternionf().rotateY(angle),0.5f,0.5f,0.5f
        );
        poseStack.scale(scale,1,1);
        poseStack.translate(-0.5,0,0);
        // 線分上に描画
        for (int i = 0; i < divideCount; i++) {
            poseStack.translate(1,0,0);
            int visualLight = calcLight(getVisualPosLight(
                instance.level, entityPos.above().getCenter().lerp(linePos.above().getCenter(),(i+0.5)/divideCount)), combinedLight);

            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null,
                Minecraft.getInstance().getModelManager().getModel(DEFAULT_LINE),
                1f,1f,1f, visualLight, combinedOverlay
            );
        }
        poseStack.popPose();
    }

    // 同心円の描画
    private void renderCircle(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, Minecraft instance, BlockPos entityPos, double circleRadius) {
        // 円周長
        float circumLength = (float) ((circleRadius + 0.125) * 2 * Math.PI);
        // 円周の長さから配列数を決める
        int divideCount = (int)Math.ceil(circumLength / 4) * 4;
        float scale = circumLength / divideCount;
        // 円周上に描画
        for (int i = 0; i < divideCount; i++) {
            float angle = (float) (i * 2 * Math.PI/divideCount);
            poseStack.pushPose();
            poseStack.translate(0,1, circleRadius);
            poseStack.rotateAround(
                new Quaternionf().rotateY(angle), 0.5f, 0.5f, 0.5f - (float) circleRadius);
            poseStack.translate((1-scale)*0.5, 0,0);
            poseStack.scale(scale,1,1);
            int visualLight = calcLight(getVisualPosLight(
                instance.level,new Vec3(Math.sin(angle) * circleRadius,1,
                    Math.cos(angle) * circleRadius).add(entityPos.getCenter())), combinedLight);
            blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null,
                Minecraft.getInstance().getModelManager().getModel(DEFAULT_CIRCLE),
                1f,1f,1f, visualLight, combinedOverlay
            );
            poseStack.popPose();
        }
    }

    //ブロックの光レベルの取得
    private static int calcLight(int combinedLight, int levelLight){
        int skyLight = combinedLight >> 20 & 15;
        int blockLight = combinedLight >> 4 & 15;
        //計算
        int maxBlockLight = Math.max(blockLight, levelLight);
        return (skyLight << 20| maxBlockLight << 4);
    }

    protected int getVisualPosLight(@Nullable ClientLevel level, Vec3 visualPos) {
        if(level == null){
            return 0;
        }
        //Vec3 visualPos = getVisualPos();
        BlockPos blockpos = BlockPos.containing(visualPos.x,visualPos.y,visualPos.z);
        return level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
    }
}
