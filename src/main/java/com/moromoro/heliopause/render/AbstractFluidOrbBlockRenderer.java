package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.AbstractFluidOrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashMap;

public abstract class AbstractFluidOrbBlockRenderer<T extends AbstractFluidOrbBlockEntity> implements BlockEntityRenderer<T> {
    public AbstractFluidOrbBlockRenderer(BlockEntityRendererProvider.Context context){
    }

    protected static HashMap<BlockPos,FluidStack> fluidList = new HashMap<>();

    public static void updateData(BlockPos pos, FluidStack updateStack) {
        if(updateStack.getAmount() == 0){removeData(pos); return;}

        if (fluidList.containsKey(pos)){
            fluidList.replace(pos,updateStack);
        }else{
            fluidList.put(pos,updateStack);
        }
    }
    public static void removeData(BlockPos pos){
        fluidList.remove(pos);
    }

    @Override
    public void render(T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = fluidList.getOrDefault(entity.getBlockPos(), FluidStack.EMPTY);
        //タンクが空なら描画処理を完了
        if (fluidStack==null||fluidStack.isEmpty())
        {
            entity.setSmoothedTankAmount(0f);
            // 現在のフレーム時間を保存
            entity.setLastFrameTime(System.nanoTime());

            return;
        }

        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        // デルタ時間を計算（秒単位）
        float deltaTime = (currentFrameTime - entity.getLastFrameTime()) / 1_000_000_000.0F;

        //内容量の見た目スムージングを計算
        entity.setSmoothedTankAmount(Math.max(0.01f,Math.lerp(entity.getSmoothedTankAmount(), fluidStack.getAmount(), Math.min(1.0f,deltaTime * 15f))));

        //タンクの割合を計算
        float fillPercentage = Math.min(1.0f,entity.getSmoothedTankAmount() / entity.getTankCapacity(0));

        //タンクの割合から、オーブのサイズを計算
        //オーブのサイズを格納
        float orbSize = (float) calcSize(getMinOrbSize(),getMaxOrbSize(),fillPercentage);

        //回転オフセットに加算
        entity.setRotationOffset(entity.getRotationOffset() + (deltaTime / orbSize) * getRotationSpeed());
        //液体を出し入れしたときに回転を加算
        entity.setRotationOffset(entity.getRotationOffset() + deltaTime * java.lang.Math.abs(entity.getSmoothedTankAmount() - fluidStack.getAmount()) * -0.25f);
        //上下動オフセットに加算
        entity.setWaveOffset(entity.getWaveOffset() + (deltaTime / orbSize) * 170f);

        //渡すデータをつくる
        HashMap<String,Object> renderingRequires = new HashMap<>();
        renderingRequires.put("fluidStack",fluidStack);
        renderingRequires.put("orbSize",orbSize);
        renderingRequires.put("rotationOffset", entity.getRotationOffset());
        renderingRequires.put("waveOffset", entity.getWaveOffset());
        renderingRequires.put("combinedOffset",entity.centerOffset().add(new Vec3(0,calcOffsetY(orbSize),0)));
        renderingRequires.put("combinedLight", combinedLight);

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        //液体の見た目をつくるメソッドを呼び出す
        renderFluid(poseStack, bufferSource, renderingRequires);
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();

        // 現在のフレーム時間を保存
        entity.setLastFrameTime(currentFrameTime);
    }

    public float getRotationSpeed() {
        return -40f;
    }

    public double calcSize(double sizeMin, double sizeMax,double fillRatio) {
        //return Math.clamp(0,sizeMax,Math.sqrt(Math.sqrt(((float) java.lang.Math.pow(sizeMax, 4)) * fillPercentage)));
        float power = 4f;
        double size = (sizeMin + (sizeMax-sizeMin) * (java.lang.Math.pow(Math.clamp(0,1,fillRatio),1f/power)));
        if(size == 0){
            Heliopause.LOGGER.error("Rendering failure on orb size calculation.");
        }
        return size;
    }

    //液球の最大サイズを指定
    public float getMaxOrbSize(){
        return 2f;
    }
    //液球の最小サイズを指定
    public float getMinOrbSize(){
        return 0f;
    }
    //液球の大きさに対する高さ方向のオフセットを計算
    public float calcOffsetY(float orbSize) {
        return 0f;
    }

    protected static void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource, HashMap<String,Object> renderingRequires)
    {
        //値を取り出す
        FluidStack fluidStack = (FluidStack) renderingRequires.get("fluidStack");
        int combinedLight = (int) renderingRequires.get("combinedLight");

        //レンダリング形式を決める
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        //(ながれる)液体テクスチャの取得
        TextureAtlasSprite sprite = getFluidSprite(fluidStack);
        //液体のtintカラーの取得
        float[] color = getFluidColor(fluidStack);
        //ブロックの光レベルの取得
        int light = calcLight(combinedLight,fluidStack);

        //配列に値を格納
        renderingRequires.put("matrix", poseStack.last().pose());
        renderingRequires.put("consumer", consumer);
        renderingRequires.put("sprite", sprite);
        renderingRequires.put("color", color);
        renderingRequires.put("light",light);

        //メッシュを組み立てる

        //配列から値を取り出す
        float orbSize = (float)renderingRequires.get("orbSize");
        float rotationOffset = (float)renderingRequires.get("rotationOffset");//entity.rotationOffset;
        float waveOffset = (float)renderingRequires.get("waveOffset");//entity.waveOffset;

        //必要な座標を用意
        final float topY = orbSize * ((float) Math.sqrt(1.5))/2, sideX = orbSize * Math.sqrt(1f/3f), sideY = topY/3;
        //場合分け
        //方向1 2 3
        for (int i = 0; i < 3; i++) {
            //上面
            Vector3f[] vertPos0 = {
                    new Vector3f(0,topY+CreateCosWaveform(waveOffset+25f, orbSize *0.03f),0),
                    new Vector3f(CreateCosWaveform(60+i*120+rotationOffset,sideX),sideY +CreateCosWaveform(waveOffset, orbSize *0.03f), CreateSinWaveform(60+i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(i*120+rotationOffset,sideX),-sideY+CreateCosWaveform(waveOffset-25f, orbSize *0.03f), CreateSinWaveform(i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(-60+i*120+rotationOffset,sideX),sideY+CreateCosWaveform(waveOffset, orbSize *0.03f), CreateSinWaveform(-60+i*120+rotationOffset,sideX))
            };
            Vector2f[] vertUV0 = {
                    new Vector2f(8-(orbSize *4),8),
                    new Vector2f(8,8+(orbSize *4)),
                    new Vector2f(8+(orbSize *4),8),
                    new Vector2f(8,8-(orbSize *4))
            };
            //メッシュを定義するメソッドを呼び出す
            renderQuads(renderingRequires,vertPos0,vertUV0);

            //下面
            Vector3f[] vertPos1 = {
                    new Vector3f(CreateCosWaveform(60+i*120+rotationOffset,sideX),sideY +CreateCosWaveform(waveOffset, orbSize *0.03f), CreateSinWaveform(60+i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(120+i*120+rotationOffset,sideX),-sideY +CreateCosWaveform(waveOffset-25f, orbSize *0.03f),CreateSinWaveform(120+i*120+rotationOffset,sideX)),
                    new Vector3f(0,-topY +CreateCosWaveform(waveOffset-50f, orbSize *0.03f),0),
                    new Vector3f(CreateCosWaveform(i*120+rotationOffset,sideX),-sideY +CreateCosWaveform(waveOffset-25f, orbSize *0.03f),CreateSinWaveform(i*120+rotationOffset,sideX))
            };
            Vector2f[] vertUV1 = {

                    new Vector2f(8-(orbSize *4),8),
                    new Vector2f(8,8+(orbSize *4)),
                    new Vector2f(8+(orbSize *4),8),
                    new Vector2f(8,8-(orbSize *4))
            };
            //メッシュを定義するメソッドを呼び出す
            renderQuads(renderingRequires,vertPos1,vertUV1);
        }
    }

    //(ながれる)液体テクスチャの取得
    private static TextureAtlasSprite getFluidSprite(@NotNull FluidStack fluidStack){
        ResourceLocation fluidTexture = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getFlowingTexture(fluidStack);
        //例外処理
        if (fluidTexture == null) {
            Heliopause.LOGGER.debug("Rendering failure on getting Fluid Sprite.");
            // テクスチャが存在しない場合はエラーテクスチャを返す
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(new ResourceLocation("minecraft", "missing_texture"));
        }
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTexture);
    }

    //液体のtintカラーの取得
    private static float[] getFluidColor(@NotNull FluidStack fluidStack) {
        int color = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor();
        //カラーデータを変換
        //alpha *= (color >> 24 & 255) / 255f;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;
        return new float[] {red,green,blue};
    }

    //ブロックの光レベルの取得
    private static int calcLight(int combinedLight, @NotNull FluidStack fluidStack){
        int skyLight = combinedLight >> 20 & 15;
        int blockLight = combinedLight >> 4 & 15;
        //液体の明るさの取得
        int fluidLight = fluidStack.getFluid().getFluidType().getLightLevel();
        //計算
        int maxBlockLight =Math.max(blockLight, fluidLight);
        return (skyLight << 20| maxBlockLight << 4);
    }

    //アニメーション用の波形をつくる
    protected static float CreateSinWaveform(float waveOffset,float amplitude){
        return (amplitude * Math.sin(Math.toRadians(waveOffset)));
    }
    protected static float CreateCosWaveform(float waveOffset, float amplitude){
        return (amplitude * Math.cos(Math.toRadians(waveOffset)));
    }

    protected static void renderQuads(HashMap<String,Object> renderingRequires, Vector3f[] vertexPos, Vector2f[] vertexUV)
    {
        //配列から値を取り出す
        Matrix4f matrix=(Matrix4f) renderingRequires.get("matrix");
        VertexConsumer buffer=(VertexConsumer) renderingRequires.get("consumer");
        TextureAtlasSprite sprite=(TextureAtlasSprite) renderingRequires.get("sprite");
        float[] color =(float[]) renderingRequires.get("color");
        int light=(int) renderingRequires.get("light");
        Vec3 offset =(Vec3) renderingRequires.get("combinedOffset");

        //色を取り出す
        float red = color[0];
        float green = color[1];
        float blue = color[2];

        //頂点を決める
        for (int i = 0; i < 4; i++) {
            buffer.vertex(matrix, 0.5f+vertexPos[i].x + (float) offset.x, 0.5f+vertexPos[i].y + (float) offset.y, 0.5f+vertexPos[i].z + (float) offset.z)
                    .color(red, green, blue, 1f)
                    .uv(sprite.getU(vertexUV[i].x),sprite.getV(vertexUV[i].y))
                    .uv2(light).normal(0, 1, 0).endVertex();
        }
    }
}
