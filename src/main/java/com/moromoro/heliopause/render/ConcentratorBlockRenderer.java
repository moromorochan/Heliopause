package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.heliopause.block.ConcentratorBlock;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class ConcentratorBlockRenderer<T extends ConcentratorBlockEntity> implements BlockEntityRenderer<T> {



    public ConcentratorBlockRenderer(BlockEntityRendererProvider.Context context){
        super();

    }

    @Override
    public void render(@NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if(entity.getBlockState().getValue(ConcentratorBlock.ENABLED).equals(false)){
        }

        // 角度を取得
        //double yaw= entity.getYaw();
        //double pitch = entity.getPitch() - Math.PI * 0.5;
        //double weightPitch = (entity.getPitch()) * 0.5f - Math.PI * 0.25;

        //renderPitch(entity, yaw, pitch, new Vec3(0,27f/16f,0), partialTicks, poseStack, bufferSource);
        //renderTurnTable(yaw, new Vec3(0,1f,0), partialTicks, poseStack, bufferSource);
        //renderWeight(yaw, weightPitch, new Vec3(0,27f/16f,0), partialTicks, poseStack, bufferSource);
    }
}
