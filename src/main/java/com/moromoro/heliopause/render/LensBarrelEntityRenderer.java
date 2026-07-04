package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.heliopause.entity.LensBarrelEntity;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.List;

public class LensBarrelEntityRenderer extends EntityRenderer<LensBarrelEntity> {
    private final BlockRenderDispatcher blockRenderer;

    // 鏡筒と連動する主要部分
    private final BakedModel pitchBottomModel;
    private final BakedModel pitchMiddleModel;
    private final BakedModel pitchTopModel;
    // 鏡筒の長さに合わせて追加する部分
    private final BakedModel pitchUpperExtendHalfModel;
    private final BakedModel pitchUpperExtendFullModel;
    private final BakedModel pitchLowerExtendHalfModel;
    private final BakedModel pitchLowerExtendFullModel;
    // ターンテーブルと連動する主要部分
    private final BakedModel turntableBottomModel;
    private final BakedModel turntableMiddleModel;
    private final BakedModel turntableGearModel;
    // 鏡筒の長さに合わせて追加する部分
    private final BakedModel turntableExtendHalfModel;
    private final BakedModel turntableExtendFullModel;
    //private final BakedModel weightModel;

    public LensBarrelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();

        this.pitchBottomModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_BOTTOM);
        this.pitchMiddleModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_MIDDLE);
        this.pitchTopModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_TOP);
        this.pitchUpperExtendHalfModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_UPPER_EX_HALF);
        this.pitchUpperExtendFullModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_UPPER_EX_FULL);
        this.pitchLowerExtendHalfModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_LOWER_EX_HALF);
        this.pitchLowerExtendFullModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_CYLINDER_LOWER_EX_FULL);

        this.turntableBottomModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_TURNTABLE_BOTTOM);
        this.turntableMiddleModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_TURNTABLE_MIDDLE);
        this.turntableGearModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_TURNTABLE_GEAR);
        this.turntableExtendHalfModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_TURNTABLE_EX_HALF);
        this.turntableExtendFullModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.CONCENTRATOR_TURNTABLE_EX_FULL);
        //this.weightModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.STARLIGHT_CONCENTRATOR_WEIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull LensBarrelEntity entity) {
        return null;
    }

    @Override
    public void render(@NotNull LensBarrelEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight) {

        final float entityYRot = entity.getPartialYRot(partialTicks);
        final float entityXRot = entity.getPartialXRot(partialTicks);
        final int cylinderLength = entity.getBarrels().size();

        double yaw = (float) Math.toRadians(180 - entityYRot);
        double pitch = (float) Math.toRadians(270 - entityXRot);
        //double weightPitch = (float) Math.toRadians(-45 - entityXRot * 0.5);

        renderBarrel(entity.getBarrels(), yaw, pitch, new Vec3(0,11f/16f,0), partialTicks, poseStack, bufferSource, combinedLight);
        renderTurnTable(cylinderLength, yaw, pitch, new Vec3(0,0,0), partialTicks, poseStack, bufferSource, combinedLight);
        //renderWeight(yaw, weightPitch, new Vec3(0,11f/16f,0), partialTicks, poseStack, bufferSource);
    }

    private void renderBarrel(List<BlockState> barrels, double yaw, double pitch, Vec3 offset, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight) {
        final int cylinderLength = barrels.size();
        final double offsetHeight = cylinderLength / 2.0;
        poseStack.pushPose();
        poseStack.translate(offset.x(),offset.y() + offsetHeight - (9.0/16.0),offset.z());
        poseStack.mulPose(new Quaternionf().rotateY((float) yaw));
        poseStack.mulPose(new Quaternionf().rotateX((float) pitch));
        poseStack.translate(-0.5,-0.5,-0.5);
        // 鏡筒
        poseStack.translate(0,-offsetHeight + 0.5,0);
        for (BlockState barrel : barrels) {
            blockRenderer.renderSingleBlock(barrel, poseStack, bufferSource, combinedLight, 0, ModelData.EMPTY, RenderType.cutout());
            poseStack.translate(0,1,0);
        }
        poseStack.translate(0,-offsetHeight - 0.5,0);

        // 中央パーツ
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchMiddleModel,
                1,1,1, combinedLight, 0
        );
        // 下端パーツ
        poseStack.translate(0,-offsetHeight + 1.0,0);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchBottomModel,
                1,1,1, combinedLight, 0
        );
        // 上端パーツ
        poseStack.translate(0,cylinderLength - 2.0,0);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchTopModel,
                1,1,1, combinedLight, 0
        );
        poseStack.translate(0,-offsetHeight + 1.0,0);

        if(cylinderLength%2 != 0){
            // 半パーツ
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchLowerExtendHalfModel,
                    1,1,1, combinedLight, 0
            );
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchUpperExtendHalfModel,
                    1,1,1, combinedLight, 0
            );
        }
        // 上下の伸長パーツ
        for (double i = (cylinderLength%2)/2.0; i < offsetHeight - 1.5; i++) {
            poseStack.translate(0,-i,0);
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchLowerExtendFullModel,
                    1,1,1, combinedLight, 0
            );
            poseStack.translate(0, i * 2,0);
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchUpperExtendFullModel,
                    1,1,1, combinedLight, 0
            );
            poseStack.translate(0,-i,0);
        }
        
        // デバッグ描画 視線
        if (Minecraft.getInstance().options.renderDebug) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINE_STRIP);
            poseStack.translate(0.5,0.5,0.5);
            buffer.vertex(poseStack.last().pose(), 0, 0, 0)
                .color(255, 0, 0, 255)
                .normal(0,0,0)
                .endVertex();
            poseStack.translate(0, 20,0);
            buffer.vertex(poseStack.last().pose(), 0, 0, 0)
                .color(255, 0, 0, 255)
                .normal(0,0,0)
                .endVertex();
        }
        poseStack.popPose();
    }

    /*private void renderWeight(double yaw, double pitch, Vec3 offset, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.translate(offset.x(),offset.y(),offset.z());
        poseStack.mulPose(new Quaternionf().rotateY((float) yaw));
        poseStack.mulPose(new Quaternionf().rotateX((float) pitch));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, weightModel,
            1,1,1, combinedLight, 0
        );
        poseStack.popPose();
    }*/

    private void renderTurnTable(int cylinderLength, double yaw, double pitch, Vec3 offset, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight) {
        poseStack.pushPose();
        poseStack.translate(offset.x(),offset.y(),offset.z());
        poseStack.mulPose(new Quaternionf().rotateY((float) yaw));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, turntableBottomModel,
            1,1,1, combinedLight, 0
        );
        poseStack.translate(0,2.0/16.0,0);
        final double offsetHeight = cylinderLength / 2.0;
        for (double i = 0; i < offsetHeight - 1.5; i++) {
            poseStack.translate(0,1.0,0);
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, turntableExtendFullModel,
                    1,1,1, combinedLight, 0
            );
        }
        if(cylinderLength%2 != 0){
            poseStack.translate(0,1.0,0);
            // 半パーツ
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, turntableExtendHalfModel,
                    1,1,1, combinedLight, 0
            );
            poseStack.translate(0,-0.5,0);
        }
        poseStack.translate(0,1.0,0);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, turntableMiddleModel,
                1,1,1, combinedLight, 0
        );
        poseStack.popPose();

        renderMotor((float) yaw, (float) pitch, offset, new Vec3(-11.5/16.0, offsetHeight -7.5/16.0,0), -30, poseStack, bufferSource, combinedLight);
        renderMotor((float) yaw, (float) pitch, offset, new Vec3(-11.5/16.0,offsetHeight + 11.5/16.0,0), 30, poseStack, bufferSource, combinedLight);

    }

    private void renderMotor(float yaw, float pitch, Vec3 tableOffset, Vec3 offset, float rotateRatio, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight) {
        poseStack.pushPose();
        poseStack.translate(tableOffset.x(), tableOffset.y(), tableOffset.z());
        poseStack.mulPose(new Quaternionf().rotateY(yaw));
        poseStack.translate(offset.x(), offset.y(), offset.z());
        poseStack.mulPose(new Quaternionf().rotateZ(pitch * rotateRatio));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, turntableGearModel,
                1,1,1, combinedLight, 0
        );
        poseStack.popPose();
    }

}
