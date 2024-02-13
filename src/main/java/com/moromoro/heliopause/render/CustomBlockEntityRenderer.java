package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface CustomBlockEntityRenderer<T extends BlockEntity> {

    static <T extends BlockEntity> BlockEntityRenderer<T> of(CustomBlockEntityRenderer<T> customRenderer){
        return customRenderer::render;
    }

    void render(T entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay);
}
