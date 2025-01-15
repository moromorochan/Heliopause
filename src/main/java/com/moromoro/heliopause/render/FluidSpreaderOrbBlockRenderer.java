package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Math;

public class FluidSpreaderOrbBlockRenderer<T extends FluidSpreaderOrbBlockEntity> implements BlockEntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderer;

    private static final float rotationX = (float) (0.196 * Math.PI);//45.0000f;
    private static final float rotationZ = (float) (0.250 * Math.PI);//35.2644f;

    public FluidSpreaderOrbBlockRenderer(BlockEntityRendererProvider.Context context) {
        super();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
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
    public void render(@NotNull FluidSpreaderOrbBlockEntity entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //アイテムを取得
        ItemStack centerItem = entity.getCenterItem();
        if(!centerItem.isEmpty()){
            // 現在のフレーム時間を取得
            long currentFrameTime = System.nanoTime();
            // デルタ時間を計算（秒単位）
            float deltaTime = (currentFrameTime - entity.getLastFrameTime()) / 1_000_000_000.0F;

            //オーブのサイズを格納
            float orbSize = (float) 1;
            //回転オフセットを取得
            float rotationOffset = entity.getRotationOffset();
            //上下動オフセットを取得
            float waveOffset = entity.getWaveOffset();

            //回転オフセットに加算
            entity.setRotationOffset(rotationOffset + (deltaTime / orbSize) * getRotationSpeed());
            //上下動オフセットに加算
            entity.setWaveOffset(waveOffset + (deltaTime / orbSize) * 170f);

            //オフセットを使用する形に変形
            rotationOffset = (float) (-rotationOffset*(Math.PI/180));
            waveOffset = (orbSize*0.03f * Math.cos(Math.toRadians(waveOffset)));

            //座標系開始
            poseStack.pushPose();

            // 中心をブロックの中心に合わせる
            poseStack.translate(0.5, (7f/16f) + waveOffset, 0.5);
            //ブロックアイテムの場合
            if(centerItem.getItem() instanceof BlockItem blockItem){
                //Block block = blockItem.getBlock();
                //BlockState blockState = block.defaultBlockState();
                //スケール調整
                poseStack.scale(0.8f,0.8f,0.8f);
                //傾きの設定
                poseStack.mulPose(new Quaternionf().rotateTo(1,1,1,0,1,0));
                //回転
                poseStack.mulPose(new Quaternionf().rotateAxis(rotationOffset,1,1,1));
                //描画
                //blockRenderer.renderSingleBlock(blockState,poseStack,bufferSource,combinedLight,combinedOverlay,net.minecraftforge.client.model.data.ModelData.EMPTY, null);
                itemRenderer.renderStatic(centerItem,ItemDisplayContext.NONE,combinedLight,combinedOverlay,poseStack,bufferSource,entity.getLevel(),0);
            }
            else
            {
                //スケール調整
                poseStack.scale(0.8f,0.8f,0.8f);
                //回転
                poseStack.mulPose(new Quaternionf().rotateY(rotationOffset));
                //描画
                itemRenderer.renderStatic(centerItem,ItemDisplayContext.FIXED,combinedLight,combinedOverlay,poseStack,bufferSource,entity.getLevel(),0);
            }

            poseStack.popPose();

            // 現在のフレーム時間を保存
            entity.setLastFrameTime(currentFrameTime);
        }
    }

    public float getRotationSpeed() {
        return -40f;
    }
}
