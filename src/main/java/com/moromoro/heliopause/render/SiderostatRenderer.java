package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.heliopause.block.SiderostatBlock;
import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class SiderostatRenderer<T extends SiderostatBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;
    public SiderostatRenderer(BlockEntityRendererProvider.Context context) {
        //super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(@NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //super.render(entity, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        Minecraft instance = Minecraft.getInstance();
        if(instance.level!=null){
            // 描画用角度
            float currentBowAngle = 0;
            // ブロックステートを取得して起動状態か確認
            BlockState blockState = instance.level.getBlockState(entity.getBlockPos());
            if(blockState.getBlock() instanceof SiderostatBlock) {
                if(blockState.getValue(SiderostatBlock.FACING_SIDEROSTAT)== Direction.UP){
                    if(entity.getSynced()){
                        // 角度を更新
                        currentBowAngle = instance.level.getSunAngle(partialTicks) + (float) (Math.toRadians(90));
                    }else{
                        // ゼンマイの角度を採用
                        currentBowAngle = (float) Math.toRadians(entity.getSpringAmount() + 180 - (Math.ceil(entity.getSpringCharge() / 2.0) /** (1.0 - partialTicks)*/));
                    }

                    // 時刻に合わせて回転
                    poseStack.pushPose();
                    renderBow(poseStack, bufferSource, currentBowAngle, combinedLight, combinedOverlay);
                    poseStack.popPose();
                }
                // モーター描画
                poseStack.pushPose();
                renderMotor(poseStack, bufferSource, currentBowAngle, combinedLight, combinedOverlay);
                poseStack.popPose();
            }
        }
    }

    private void renderBow(PoseStack poseStack, MultiBufferSource bufferSource, float currentMoonAngle, int combinedLight, int combinedOverlay) {
        // 描画するモデル元
        BlockState bowBlockState = BlockRegistry.SIDEROSTAT_TOP.get().defaultBlockState();
        // 角度を計算
        //float currentMoonAngle = level.getSunAngle(partialTicks) + (float) (Math.toRadians(90));

        poseStack.translate(0.5,0.5,0.5);
        poseStack.mulPose(new Quaternionf().rotateZ(currentMoonAngle));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.renderSingleBlock(bowBlockState, poseStack, bufferSource, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.cutout());
    }

    private void renderMotor(PoseStack poseStack, MultiBufferSource bufferSource, float currentMoonAngle, int combinedLight, int combinedOverlay) {
        // 描画するモデル元
        BlockState bowBlockState = BlockRegistry.SIDEROSTAT_MOTOR.get().defaultBlockState();
        // 角度を計算
        //float currentMoonAngle = level.getSunAngle(partialTicks)*10;

        poseStack.translate(0.5,-0.5,0.5);
        poseStack.mulPose(new Quaternionf().rotateZ(currentMoonAngle*10));
        poseStack.translate(-0.5,-0.5,-0.5);
        blockRenderer.renderSingleBlock(bowBlockState, poseStack, bufferSource, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.cutout());
    }

}
