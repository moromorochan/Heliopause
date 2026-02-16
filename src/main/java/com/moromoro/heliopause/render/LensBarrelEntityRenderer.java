package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
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
    private final BakedModel pitchModel;
    private final BakedModel turntableModel;
    private final BakedModel weightModel;

    public LensBarrelEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.pitchModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.STARLIGHT_CONCENTRATOR_PITCH);
        this.turntableModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.STARLIGHT_CONCENTRATOR_TURNTABLE);
        this.weightModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.STARLIGHT_CONCENTRATOR_WEIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull LensBarrelEntity entity) {
        return null;
    }

    @Override
    public void render(@NotNull LensBarrelEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight) {

        float entityYRot = entity.getPartialYRot(partialTicks);
        float entityXRot = entity.getPartialXRot(partialTicks);

        double yaw = (float) Math.toRadians(180 - entityYRot);
        double pitch = (float) Math.toRadians(270 - entityXRot);
        double weightPitch = (float) Math.toRadians(-45 - entityXRot * 0.5);

        renderBarrel(entity.getBarrels(), yaw, pitch, new Vec3(0,11f/16f,0), partialTicks, poseStack, bufferSource);
        renderTurnTable(yaw, new Vec3(0,0,0), partialTicks, poseStack, bufferSource);
        renderWeight(yaw, weightPitch, new Vec3(0,11f/16f,0), partialTicks, poseStack, bufferSource);
    }

    private void renderBarrel(List<BlockState> barrels, double yaw, double pitch, Vec3 offset, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.translate(offset.x(),offset.y(),offset.z());
        poseStack.mulPose(new Quaternionf().rotateY((float) yaw));
        poseStack.mulPose(new Quaternionf().rotateX((float) pitch));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, pitchModel,
            1,1,1, 0xF000F0, 0
        );

        poseStack.translate(0,-2f/16f,0);
        for (BlockState barrel : barrels) {
            blockRenderer.renderSingleBlock(barrel, poseStack, bufferSource, 0xF000F0, 0, ModelData.EMPTY, RenderType.cutout());
            poseStack.translate(0,1,0);
        }
        poseStack.popPose();
    }

    private void renderWeight(double yaw, double pitch, Vec3 offset, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.translate(offset.x(),offset.y(),offset.z());
        poseStack.mulPose(new Quaternionf().rotateY((float) yaw));
        poseStack.mulPose(new Quaternionf().rotateX((float) pitch));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, weightModel,
            1,1,1, 0xF000F0, 0
        );
        poseStack.popPose();
    }

    private void renderTurnTable(double yaw, Vec3 offset, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.translate(offset.x(),offset.y(),offset.z());
        poseStack.mulPose(new Quaternionf().rotateY((float) yaw));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, turntableModel,
            1,1,1, 0xF000F0, 0
        );
        poseStack.popPose();
    }

}
