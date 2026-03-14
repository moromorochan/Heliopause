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
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        if (!permanentRender(entity)&&(fluidStack==null||fluidStack.isEmpty()))
        {
            entity.setSmoothedTankAmount(0f);
            // 現在のフレーム時間を保存
            entity.setLastFrameTime(System.nanoTime());
            return;
        }

        //渡すデータをつくる
        HashMap<String,Object> renderingRequires = new HashMap<>();
        renderingRequires.put("fluidStack",fluidStack);

        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        // デルタ時間を計算（秒単位）
        float deltaTime = (currentFrameTime - entity.getLastFrameTime()) / 1_000_000_000.0F;

        //時間依存アニメーションに必要な情報を入れる
        putProperties(entity,deltaTime,renderingRequires);

        renderingRequires.put("combinedLight", combinedLight);

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        //液体の見た目をつくるメソッドを呼び出す
        renderGroup(poseStack, bufferSource, renderingRequires);
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();

        // 現在のフレーム時間を保存
        entity.setLastFrameTime(currentFrameTime);
    }

    protected void putProperties(T entity, float deltaTime, HashMap<String, Object> renderingRequires) {

        FluidStack fluidStack = (FluidStack) renderingRequires.get("fluidStack");
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

        renderingRequires.put("orbSize",orbSize);
        renderingRequires.put("rotationOffset", entity.getRotationOffset() + RandomSource.create(entity.getBlockPos().asLong()).nextInt(0,360));
        renderingRequires.put("waveOffset", entity.getWaveOffset());
        renderingRequires.put("combinedOffset",entity.centerOffset().add(new Vec3(0,calcOffsetY(orbSize),0)));
    }

    protected boolean permanentRender(T entity) {
        return false;
    }

    public void renderGroup(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, HashMap<String, Object> renderingRequires){
        renderFluid(poseStack,bufferSource,renderingRequires);
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

        //配列に値を格納
        renderingRequires.put("matrix", poseStack.last().pose());
        renderingRequires.put("consumer", consumer);
        renderingRequires.put("sprite", sprite);
        renderingRequires.put("color", color);

        //メッシュを組み立てる

        //配列から値を取り出す
        float orbSize = (float)renderingRequires.get("orbSize");
        float rotationOffset = (float)renderingRequires.get("rotationOffset");//entity.rotationOffset;
        float waveOffset = (float)renderingRequires.get("waveOffset");//entity.waveOffset;

        //必要な座標を用意
        final float topY = orbSize * ((float) Math.sqrt(1.5))/2, sideX = orbSize * Math.sqrt(1f/3f), sideY = topY/3;

        //ドット数を用意
        int pixel_num = Math.max(1, Math.round(16*(orbSize/Math.sqrt(2))));
        //角度とドット数からテクスチャのずれを設定
        float pixel_offset = (-rotationOffset * Math.ceil(pixel_num * 2/16f) *16) / 360f;

        //必要な座標の設定
        //面全体
        Vector3f topVertex0 = new Vector3f(0,topY,0);
        Vector3f topVertex1 = new Vector3f(CreateCosWaveform(60,sideX), sideY, CreateSinWaveform(60,sideX)).rotateY(Math.toRadians(-rotationOffset));
        Vector3f topVertex2 = new Vector3f(sideX, -sideY, 0).rotateY(Math.toRadians(-rotationOffset));
        Vector3f topVertex3 = new Vector3f(CreateCosWaveform(-60,sideX), sideY, CreateSinWaveform(-60,sideX)).rotateY(Math.toRadians(-rotationOffset));

        Vector3f bottomVertex0 = new Vector3f(CreateCosWaveform(60,sideX),sideY, CreateSinWaveform(60,sideX)).rotateY(Math.toRadians(-rotationOffset));
        Vector3f bottomVertex1 = new Vector3f(CreateCosWaveform(120,sideX),-sideY,CreateSinWaveform(120,sideX)).rotateY(Math.toRadians(-rotationOffset));
        Vector3f bottomVertex2 = new Vector3f(0,-topY,0);
        Vector3f bottomVertex3 = new Vector3f(sideX,-sideY,0).rotateY(Math.toRadians(-rotationOffset));

        //スケール調整
        poseStack.translate(0, CreateCosWaveform(waveOffset+50, -topY * 0.02f),0);
        poseStack.scale(1,1 + CreateCosWaveform(waveOffset+50, orbSize *0.02f),1);

        //位置調整
        poseStack.translate(0.5f,0.5f+ CreateSinWaveform(waveOffset, orbSize *0.02f),0.5f);

        //フチ

        //テクスチャの境目

        //場合分け
        //方向1 2 3
        for (int i = 0; i < 3; i++) {
            //上面
            Vector3f[] vertPos0 = {
                topVertex3.rotateY(Math.toRadians(120)),
                topVertex0,
                topVertex1.rotateY(Math.toRadians(120)),
                topVertex2.rotateY(Math.toRadians(120))
            };

            //面ごとの明るさ設定
            int light0 = calcLight(combinedLight,fluidStack);
            renderingRequires.put("light",light0);
            //メッシュを定義するメソッドを呼び出す
            renderQuads(renderingRequires,vertPos0,pixel_offset,pixel_num);

            //下面
            Vector3f[] vertPos1 = {
                bottomVertex3.rotateY(Math.toRadians(120)),
                    bottomVertex0.rotateY(Math.toRadians(120)),
                    bottomVertex1.rotateY(Math.toRadians(120)),
                    bottomVertex2
            };

            //面ごとの明るさ設定
            int light1 = calcLight(combinedLight/2,fluidStack);
            renderingRequires.put("light",light1);
            //メッシュを定義するメソッドを呼び出す
            renderQuads(renderingRequires,vertPos1,pixel_offset,pixel_num);
        }
    }

    private static double calcPixelSize(float meshSize, int resolution) {
        //サイズが0なら0を返す
        if(meshSize == 0.0){
            return 0.0;
        }
        //ドット数の計算
        int divisor = Math.round(resolution * meshSize);
        //メッシュが小さすぎてドット数が0になる場合、メッシュの大きさを返す
        if(divisor == 0){
            return meshSize;
        }
        //ドットサイズを返す
        return meshSize / divisor;
    }

    //(とどまる)液体テクスチャの取得
    private static TextureAtlasSprite getFluidSprite(@NotNull FluidStack fluidStack){
        ResourceLocation fluidTexture = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);
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
    protected static float[] getFluidColor(@NotNull FluidStack fluidStack) {
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

    protected static void renderQuads(HashMap<String, Object> renderingRequires, Vector3f[] vertexPos, float pixelOffset, float pixelNum) {
        //配列から値を取り出す
        Matrix4f matrix = (Matrix4f) renderingRequires.get("matrix");
        VertexConsumer buffer = (VertexConsumer) renderingRequires.get("consumer");
        TextureAtlasSprite sprite = (TextureAtlasSprite) renderingRequires.get("sprite");
        float[] color = (float[]) renderingRequires.get("color");
        int light = (int) renderingRequires.get("light");
        Vec3 offset = (Vec3) renderingRequires.get("combinedOffset");

        //色を取得
        float red = color[0];
        float green = color[1];
        float blue = color[2];

        pixelOffset = pixelOffset % 16;

        //uvの開始位置を設定
        Vector2f uv0 = new Vector2f(
            Math.round(pixelNum / 2f) - pixelOffset,
            Math.round(pixelNum / 2f) - pixelOffset);

        // 面のローカル軸を計算
        Vector3f localAxisX = new Vector3f(vertexPos[1]).sub(vertexPos[0]).mul(1f / pixelNum);
        Vector3f localAxisY = new Vector3f(vertexPos[3]).sub(vertexPos[0]).mul(1f / pixelNum);

        //分割位置の配列を用意
        List<Float> partPosX = new ArrayList<>();
        partPosX.add(0f);
        float boundaryX = (float)(Math.ceil(uv0.x / 16.0)) * 16f - uv0.x;
        if(boundaryX > 0f && boundaryX < pixelNum){
            partPosX.add(boundaryX);
        }
        for (float b = boundaryX + 16f; b < pixelNum; b += 16f) {
            partPosX.add(b);
        }
        partPosX.add(pixelNum);

        List<Float> partPosY = new ArrayList<>();
        partPosY.add(0f);
        float boundaryY = (float)(Math.ceil(uv0.y / 16.0)) * 16f - uv0.y;
        if(boundaryY > 0f && boundaryY < pixelNum){
            partPosY.add(boundaryY);
        }
        for (float b = boundaryY + 16f; b < pixelNum; b += 16f) {
            partPosY.add(b);
        }
        partPosY.add(pixelNum);

        //各面を描画
        for (int yi = 0; yi < partPosY.size() - 1; yi++) {
            for (int xi = 0; xi < partPosX.size() - 1; xi++) {

                float relX1 = partPosX.get(xi);
                float relX2 = partPosX.get(xi + 1);
                float relY1 = partPosY.get(yi);
                float relY2 = partPosY.get(yi + 1);

                float u1 = modUV(uv0.x + relX1);
                float u2 = modUV(uv0.x + relX2);
                if(u2<=u1){
                    u2+=16;
                }
                float v1 = modUV(uv0.y + relY1);
                float v2 = modUV(uv0.y + relY2);
                if(v2<=v1){
                    v2+=16;
                }

                Vector2f[] vertexUV = new Vector2f[4];
                vertexUV[0] = new Vector2f(u1, v1); // 左下
                vertexUV[1] = new Vector2f(u2, v1); // 左上
                vertexUV[2] = new Vector2f(u2, v2); // 右上
                vertexUV[3] = new Vector2f(u1, v2); // 右下

                Vector3f[] partVertexPos = new Vector3f[4];
                partVertexPos[0] = new Vector3f(vertexPos[0])
                    .add(new Vector3f(localAxisX).mul(relX1))
                    .add(new Vector3f(localAxisY).mul(relY1));
                partVertexPos[1] = new Vector3f(vertexPos[0])
                    .add(new Vector3f(localAxisX).mul(relX2))
                    .add(new Vector3f(localAxisY).mul(relY1));
                partVertexPos[2] = new Vector3f(vertexPos[0])
                    .add(new Vector3f(localAxisX).mul(relX2))
                    .add(new Vector3f(localAxisY).mul(relY2));
                partVertexPos[3] = new Vector3f(vertexPos[0])
                    .add(new Vector3f(localAxisX).mul(relX1))
                    .add(new Vector3f(localAxisY).mul(relY2));

                // 面を描画
                for (int i = 0; i < 4; i++) {
                    buffer.vertex(matrix,
                            partVertexPos[i].x + (float) offset.x,
                            partVertexPos[i].y + (float) offset.y,
                            partVertexPos[i].z + (float) offset.z)
                        .color(red, green, blue, 1f)
                        .uv(sprite.getU(vertexUV[i].x), sprite.getV(vertexUV[i].y))
                        .uv2(light)
                        .normal(0, 1, 0)
                        .endVertex();
                }
            }
        }
    }

    private static float modUV(float value) {
        return ((value % 16) + 16) % 16;
    }
}