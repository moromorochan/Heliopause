package com.moromoro.heliopause.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import com.moromoro.heliopause.blockEntity.OrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class OrbBlockRenderer implements BlockEntityRenderer<OrbBlockEntity> {

    //直前のフレーム描画時刻を格納
    private long lastFrameTime;
    //回転の進捗を格納
    private float rotationOffset;
    public OrbBlockRenderer(BlockEntityRendererProvider.Context context){
        //初期化
        lastFrameTime = System.nanoTime();
        rotationOffset = 0.0f;
    }

    //メッシュをあれこれするための変数を格納するクラス
    public static class meshContentContainer {
        private Matrix4f matrix;
        private VertexConsumer buffer;
        private TextureAtlasSprite sprite;
        private float r,g,b,alpha;
        private float orbSize;
        private float orbDensity;
        private int light;
        PoseStack poseStack;
        MultiBufferSource bufferSource;
        FluidStack fluidStack;
        public meshContentContainer(
                Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite,
                float r, float g, float b, float alpha,
                float orbSize,float orbDensity, int light,
                PoseStack poseStack,MultiBufferSource bufferSource, FluidStack fluidStack)
            {
                this.matrix=matrix;
                this.buffer=buffer;
                this.sprite=sprite;
                this.r=r;this.g=g;this.b=b;this.alpha=alpha;
                this.orbSize=orbSize;
                this.orbDensity=orbDensity;
                this.light=light;
                this.poseStack=poseStack;
                this.bufferSource=bufferSource;
                this.fluidStack=fluidStack;
            }
    }

    @Override
    public void render(OrbBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //タンクの液体を取得
        FluidStack fluidStack = entity.getFluidInTank(0);
        //タンクが空なら描画処理を完了
        if (fluidStack.isEmpty())
            return;
        //タンクの割合から、オーブのサイズと密度を計算
        float orbSize = 1;
        float orbDensity = 1;

        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        // デルタ時間を計算（秒単位）
        float deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0F;
        //回転オフセットに加算
        rotationOffset = 3f;//+= (deltaTime / (orbSize*orbDensity))*1000f ;
        // 現在のフレーム時間を保存
        lastFrameTime = currentFrameTime;

        //コンテナクラスにデータを格納
        meshContentContainer fluidMesh = new meshContentContainer(null,null,null,1,1,1,1,orbSize,orbDensity,1,poseStack,bufferSource,fluidStack);
        //メッシュを管理する関数を呼び出す
        //renderOrb(fluidMesh);
    }

    private static void renderOrb(meshContentContainer fluidMesh)
    {
        //コンテナから値を取り出す
        float meshSize=fluidMesh.orbSize;
        PoseStack poseStack=fluidMesh.poseStack;

        //必要な座標を用意
        final float topY = meshSize * ((float) Math.sqrt(1.5))/2, sideX = meshSize * ((float) Math.sqrt(1f/3f)), sideY = topY/3;
        //場合分け
        //方向1 2 3
        for (int i = 0; i < 3; i++) {
            //上面
            Vector3f[] vertPos0 = {
                    new Vector3f(0,topY,0),
                    new Vector3f((float) sideX*Math.cos(Math.toRadians(60+i*120)),sideY,(float) sideX*Math.sin(Math.toRadians(60+i*120))),
                    new Vector3f((float) sideX*Math.cos(Math.toRadians(i*120)),-sideY,(float) sideX*Math.sin(Math.toRadians(i*120))),
                    new Vector3f((float) sideX*Math.cos(Math.toRadians(-60+i*120)),sideY,(float) sideX*Math.sin(Math.toRadians(-60+i*120)))
            };
            Vector2f[] vertUV0 = {
                    new Vector2f(8-(meshSize*4),8),
                    new Vector2f(8,8+(meshSize*4)),
                    new Vector2f(8+(meshSize*4),8),
                    new Vector2f(8,8-(meshSize*4))};
            //親モデルをスタックに保管して、子モデルの編集をはじめる
            poseStack.pushPose();
            //液体の見た目をつくる関数を呼び出す
            renderFluid(fluidMesh,vertPos0,vertUV0);
            //親モデルをスタックから取り出して、子モデルの編集をおわる
            poseStack.popPose();

            //下面
            Vector3f[] vertPos1 = {
                    new Vector3f((float) sideX*Math.cos(Math.toRadians(60+i*120)),sideY,(float) sideX*Math.sin(Math.toRadians(60+i*120))),
                    new Vector3f((float) sideX*Math.cos(Math.toRadians(120+i*120)),-sideY,(float) sideX*Math.sin(Math.toRadians(120+i*120))),
                    new Vector3f(0,-topY,0),
                    new Vector3f((float) sideX*Math.cos(Math.toRadians(i*120)),-sideY,(float) sideX*Math.sin(Math.toRadians(i*120)))
            };
            Vector2f[] vertUV1 = {
                    new Vector2f(12-(meshSize*4),12),
                    new Vector2f(12,12+(meshSize*4)),
                    new Vector2f(12+(meshSize*4),12),
                    new Vector2f(12,12-(meshSize*4))};
            //親モデルをスタックに保管して、子モデルの編集をはじめる
            poseStack.pushPose();
            //液体の見た目をつくる関数を呼び出す
            renderFluid(fluidMesh,vertPos1,vertUV1);
            //親モデルをスタックから取り出して、子モデルの編集をおわる
            poseStack.popPose();
        }
    }

    private static void renderFluid(meshContentContainer fluidMesh, Vector3f[] vertexPos, Vector2f[] vertexUV){
        //コンテナを展開
        MultiBufferSource bufferSource=fluidMesh.bufferSource;
        FluidStack fluidStack=fluidMesh.fluidStack;
        float density= fluidMesh.orbDensity;
        //レンダリング形式を決める
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        //液体の種類を取り出す
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        //(ながれる)液体テクスチャの取得
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTypeExtensions.getFlowingTexture(fluidStack));
        //液体のtintカラーの取得
        int color = fluidTypeExtensions.getTintColor();
        //カラーデータを変換
        //alpha *= (color >> 24 & 255) / 255f;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        //コンテナクラスにデータを格納
        meshContentContainer orbMesh= new meshContentContainer(
                fluidMesh.poseStack.last().pose(),consumer, sprite,
                red,green,blue,1,
                fluidMesh.orbSize, density, fluidMesh.light, fluidMesh.poseStack,bufferSource,fluidStack);

        //メッシュをつくる関数を呼び出す
        renderQuads(orbMesh, vertexPos, vertexUV);
    }

    private static void renderQuads(meshContentContainer orbMesh, Vector3f[] vertexPos, Vector2f[] vertexUV)
    {
        //コンテナから値を取り出す
        Matrix4f matrix=orbMesh.matrix;
        VertexConsumer buffer=orbMesh.buffer;
        TextureAtlasSprite sprite=orbMesh.sprite;
        float r=orbMesh.r,g=orbMesh.g,b=orbMesh.b,alpha=orbMesh.alpha;
        float heightOffset=orbMesh.orbSize*0.5f-0.3f;
        int light=orbMesh.light;

        //頂点を決める
        for (int i = 0; i < 4; i++) {
            buffer.vertex(matrix, 0.5f+vertexPos[i].x, 0.5f+vertexPos[i].y+heightOffset, 0.5f+vertexPos[i].z)
                    .color(r, g, b, 1f)
                    .uv(sprite.getU(vertexUV[i].x),sprite.getV(vertexUV[i].y))
                    .uv2(light).normal(0, 1, 0).endVertex();
        }
    }
}
