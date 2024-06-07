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
import org.joml.Math;
import org.joml.Matrix4f;

public class CrucibleBlockRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    public CrucibleBlockRenderer(BlockEntityRendererProvider.Context context){

    }
    //ブロックの端から液面の端までの距離
    private static final float MARGIN = 2/16f;
    @Override
    public void render(CrucibleBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //タンクの液体を取得
        FluidStack fluidStack = entity.getFluidInTank(0);
        //タンクが空なら描画処理を完了
        if (fluidStack.isEmpty())
        {
            entity.smoothedTankAmount=0f;
            return;
        }
/*
        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        // デルタ時間を計算（秒単位）
        float deltaTime = (currentFrameTime - entity.lastFrameTime) / 1_000_000_000.0F;
        */
        //内容量の見た目スムージングを計算
        entity.smoothedTankAmount = Math.lerp(entity.smoothedTankAmount,fluidStack.getAmount(),0.15f);

        //液面高さの上限と下限を決める
        final float fillMax = 15f, fillMin = 6f;
        //タンクの割合から液面高さを計算
        float fillPercentage = Math.min(fillMax, fillMin + (fillMax-fillMin)*((float) entity.smoothedTankAmount / entity.getTankCapacity(0)))/16f;

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        //液体の見た目をつくる関数を呼び出す
        renderFluid(poseStack, bufferSource, fluidStack, fillPercentage, combinedLight);
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();
    }

    private static void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource, FluidStack fluidStack, float heightPercentage, int combinedLight) {
        //レンダリング形式を決める
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        //液体の種類を取り出す
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        //(とどまる)液体テクスチャの取得
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTypeExtensions.getStillTexture(fluidStack));
        //液体のtintカラーの取得
        int color = fluidTypeExtensions.getTintColor();
        //カラーデータを変換
        //alpha *= (color >> 24 & 255) / 255f;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        //メッシュをつくる関数を呼び出す
        renderQuads(poseStack.last().pose(), consumer, sprite, red, green, blue, heightPercentage, combinedLight);
    }

    private static void renderQuads(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, float r, float g, float b, float heightPercentage, int light) {
        //液面高さを取り出す
        float height = heightPercentage;
        //スプライトからuv座標の四隅を取得
        float minU = sprite.getU(MARGIN * 16), maxU = sprite.getU((1 - MARGIN) * 16);
        float minV = sprite.getV(MARGIN * 16), maxV = sprite.getV((1 - MARGIN) * 16);
        //頂点を決める
        buffer.vertex(matrix, MARGIN, height, MARGIN).color(r, g, b, 1).uv(minU, minV).uv2(light).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, MARGIN, height, 1 - MARGIN).color(r, g, b, 1).uv(minU, maxV).uv2(light).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, 1 - MARGIN, height, 1 - MARGIN).color(r, g, b, 1).uv(maxU, maxV).uv2(light).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, 1 - MARGIN, height, MARGIN).color(r, g, b, 1).uv(maxU, minV).uv2(light).normal(0, 1, 0).endVertex();
    }
}
