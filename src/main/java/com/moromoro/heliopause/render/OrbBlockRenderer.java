package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.heliopause.blockEntity.OrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.*;
import org.joml.Math;

public class OrbBlockRenderer implements BlockEntityRenderer<OrbBlockEntity> {
    public OrbBlockRenderer(BlockEntityRendererProvider.Context context){

    }

    @Override
    public void render(OrbBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //タンクの液体を取得
        FluidStack fluidStack = entity.getFluidInTank(0);
        //タンクが空なら描画処理を完了
        if (fluidStack.isEmpty())
        {
            entity.smoothedTankAmount=0f;
            // 現在のフレーム時間を保存
            entity.lastFrameTime = System.nanoTime();

            return;
        }

        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        // デルタ時間を計算（秒単位）
        float deltaTime = (currentFrameTime - entity.lastFrameTime) / 1_000_000_000.0F;

        //内容量の見た目スムージングを計算
        entity.smoothedTankAmount = Math.lerp(entity.smoothedTankAmount,fluidStack.getAmount(),deltaTime * 15f);

        //タンクの割合を計算
        final float fillMax = 2f;
        float fillPercentage = entity.smoothedTankAmount / entity.getTankCapacity(0);

        //タンクの割合から、オーブのサイズと密度を計算
        //オーブのサイズと密度を格納
        float orbSize = Math.sqrt(Math.sqrt(((float) java.lang.Math.pow(fillMax, 4)) * fillPercentage));
        float orbDensity = 1;

        //回転オフセットに加算
        entity.rotationOffset += (deltaTime / orbSize)*-40f;
        entity.rotationOffset += deltaTime * Math.abs(entity.smoothedTankAmount-fluidStack.getAmount())*-0.25f;
        entity.rotationOffset = (entity.rotationOffset%360);
        //上下動オフセットに加算
        entity.waveOffset += (deltaTime / orbSize)*170f;
        entity.waveOffset = (entity.waveOffset%360);

        // 現在のフレーム時間を保存
        entity.lastFrameTime = currentFrameTime;

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        //液体の見た目をつくる関数を呼び出す
        renderFluid(poseStack, bufferSource, fluidStack, orbDensity, combinedLight,new float[] {orbSize,entity.rotationOffset,entity.waveOffset});
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();
    }
    private static void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource, FluidStack fluidStack, float density, int combinedLight,float[] modelProperty)//(meshContentContainer fluidMesh, Vector3f[] vertexPos, Vector2f[] vertexUV)
    {
        //レンダリング形式を決める
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        //液体の種類を取り出す
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        //(ながれる)液体テクスチャの取得
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTypeExtensions.getFlowingTexture(fluidStack));
        //液体のtintカラーの取得
        int color = fluidTypeExtensions.getTintColor();

        //ブロックの光レベルの取得
        int skyLight = combinedLight >> 20 & 15;
        int blockLight = combinedLight >> 4 & 15;
        //液体の明るさの取得
        int fluidLight = fluidStack.getFluid().getFluidType().getLightLevel();
        //計算
        int maxBlockLight =Math.max(blockLight, fluidLight);
        int newCombinedLight = (skyLight << 20| maxBlockLight << 4);

        //コンテナクラスにデータを格納
        fluidMatContainer matProperty = new fluidMatContainer(poseStack.last().pose(),consumer,sprite,color,newCombinedLight);
        //メッシュを組み立てる関数を呼び出す
        renderOrb(modelProperty,matProperty);
    }
    //液体のマテリアル描画に必要なものを格納しておくコンテナ
    public static class fluidMatContainer
    {
        private Matrix4f matrix;
        private VertexConsumer consumer;
        private TextureAtlasSprite sprite;
        private int color;
        private int light;
        public fluidMatContainer(
                Matrix4f matrix, VertexConsumer consumer, TextureAtlasSprite sprite, int color,int light
        )
        {
            this.matrix=matrix;
            this.consumer=consumer;
            this.sprite=sprite;
            this.color=color;
            this.light=light;
        }
    }

    //アニメーション用の波形をつくる
    private static float CreateSinWaveform(float waveOffset,float amplitude){
        return ((float) amplitude * Math.sin(Math.toRadians(waveOffset)));
    }
    private static float CreateCosWaveform(float waveOffset, float amplitude){
        return ((float) amplitude * Math.cos(Math.toRadians(waveOffset)));
    }
    private static void renderOrb(float[] modelProperty, fluidMatContainer matProperty)
    {
        //値を取り出す
        float meshSize=modelProperty[0];
        float rotationOffset=modelProperty[1];
        float waveOffset=modelProperty[2];

        //必要な座標を用意
        final float topY = meshSize * ((float) Math.sqrt(1.5))/2, sideX = meshSize * ((float) Math.sqrt(1f/3f)), sideY = topY/3;
        float offsetY=meshSize * 0.5f-0.0f;
        //場合分け
        //方向1 2 3
        for (int i = 0; i < 3; i++) {
            //上面
            Vector3f[] vertPos0 = {
                    new Vector3f(0,topY+offsetY+CreateCosWaveform(waveOffset+25f,meshSize*0.03f),0),
                    new Vector3f(CreateCosWaveform(60+i*120+rotationOffset,sideX),sideY+offsetY +CreateCosWaveform(waveOffset,meshSize*0.03f), CreateSinWaveform(60+i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(i*120+rotationOffset,sideX),-sideY+offsetY+CreateCosWaveform(waveOffset-25f,meshSize*0.03f), CreateSinWaveform(i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(-60+i*120+rotationOffset,sideX),sideY+offsetY+CreateCosWaveform(waveOffset,meshSize*0.03f), CreateSinWaveform(-60+i*120+rotationOffset,sideX))
            };
            Vector2f[] vertUV0 = {
                    new Vector2f(8-(meshSize*4),8),
                    new Vector2f(8,8+(meshSize*4)),
                    new Vector2f(8+(meshSize*4),8),
                    new Vector2f(8,8-(meshSize*4))
            };
            //メッシュを定義する関数を呼び出す
            renderQuads(matProperty,vertPos0,vertUV0);

            //下面
            Vector3f[] vertPos1 = {
                    new Vector3f(CreateCosWaveform(60+i*120+rotationOffset,sideX),sideY+offsetY +CreateCosWaveform(waveOffset,meshSize*0.03f), CreateSinWaveform(60+i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(120+i*120+rotationOffset,sideX),-sideY+offsetY +CreateCosWaveform(waveOffset-25f,meshSize*0.03f),CreateSinWaveform(120+i*120+rotationOffset,sideX)),
                    new Vector3f(0,-topY+offsetY +CreateCosWaveform(waveOffset-50f,meshSize*0.03f),0),
                    new Vector3f(CreateCosWaveform(i*120+rotationOffset,sideX),-sideY+offsetY +CreateCosWaveform(waveOffset-25f,meshSize*0.03f),CreateSinWaveform(i*120+rotationOffset,sideX))
            };
            Vector2f[] vertUV1 = {

                    new Vector2f(8-(meshSize*4),8),
                    new Vector2f(8,8+(meshSize*4)),
                    new Vector2f(8+(meshSize*4),8),
                    new Vector2f(8,8-(meshSize*4))
            };
            //メッシュを定義する関数を呼び出す
            renderQuads(matProperty,vertPos1,vertUV1);
        }
    }

    private static void renderQuads(fluidMatContainer matProperty,Vector3f[] vertexPos, Vector2f[] vertexUV)
    {
        //コンテナから値を取り出す
        Matrix4f matrix=matProperty.matrix;
        VertexConsumer buffer=matProperty.consumer;
        TextureAtlasSprite sprite=matProperty.sprite;
        int color = matProperty.color;
        int light=matProperty.light;

        //カラーデータを変換
        //alpha *= (color >> 24 & 255) / 255f;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        //頂点を決める
        for (int i = 0; i < 4; i++) {
            buffer.vertex(matrix, 0.5f+vertexPos[i].x, 0.5f+vertexPos[i].y, 0.5f+vertexPos[i].z)
                    .color(red, green, blue, 1f)
                    .uv(sprite.getU(vertexUV[i].x),sprite.getV(vertexUV[i].y))
                    .uv2(light).normal(0, 1, 0).endVertex();
        }
    }
}
