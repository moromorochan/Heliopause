package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class CrucibleBlockRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    public CrucibleBlockRenderer(BlockEntityRendererProvider.Context context){

    }
    private static final float MARGIN = 2/16f;
    @Override
    public void render(CrucibleBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = entity.getFluidInTank(0);
        if (fluidStack.isEmpty())
            return;

        float fillPercentage = Math.min(15f, 6f + 9f*((float) fluidStack.getAmount() / entity.getTankCapacity(0)))/16f;
        poseStack.pushPose();
        renderFluid(poseStack, bufferSource, fluidStack, 1, fillPercentage, combinedLight);
        poseStack.popPose();
    }

    private static void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource, FluidStack fluidStack, float alpha, float heightPercentage, int combinedLight) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTypeExtensions.getStillTexture(fluidStack));
        int color = fluidTypeExtensions.getTintColor();
        alpha *= (color >> 24 & 255) / 255f;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        renderQuads(poseStack.last().pose(), consumer, sprite, red, green, blue, alpha, heightPercentage, combinedLight);
    }

    private static void renderQuads(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, float r, float g, float b, float alpha, float heightPercentage, int light) {
        float height = heightPercentage;
        float minU = sprite.getU(MARGIN * 16), maxU = sprite.getU((1 - MARGIN) * 16);
        sprite.getV(MARGIN * 16);
        sprite.getV(height * 16);
        float minV, maxV;
        // top
        if (heightPercentage < 1) {
            minV = sprite.getV(MARGIN * 16);
            maxV = sprite.getV((1 - MARGIN) * 16);
            buffer.vertex(matrix, MARGIN, height, MARGIN).color(r, g, b, alpha).uv(minU, minV).uv2(light).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, MARGIN, height, 1 - MARGIN).color(r, g, b, alpha).uv(minU, maxV).uv2(light).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, 1 - MARGIN, height, 1 - MARGIN).color(r, g, b, alpha).uv(maxU, maxV).uv2(light).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, 1 - MARGIN, height, MARGIN).color(r, g, b, alpha).uv(maxU, minV).uv2(light).normal(0, 1, 0).endVertex();
        }
    }
}
