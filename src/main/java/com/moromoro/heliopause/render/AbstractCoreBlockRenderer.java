package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.AbstractFluidOrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashMap;

public class AbstractCoreBlockRenderer<T extends AbstractFluidOrbBlockEntity> extends AbstractFluidOrbBlockRenderer<T> {
    public AbstractCoreBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public float getMaxOrbSize() {
        return 1f;
    }

    @Override
    public float getMinOrbSize() {
        return getUVSize() * Math.sqrt(3) * 0.5f;
    }

    protected float getUVSize() {
        return 5/16f;
    }

    @Override
    public double calcSize(double sizeMin ,double sizeMax,double fillRatio) {
        float power = 2f;
        double size = (sizeMin + (sizeMax-sizeMin) * (java.lang.Math.pow(Math.clamp(0,1,fillRatio),1f/power)));
        if(size == 0){
            Heliopause.LOGGER.error("Rendering failure on orb size calculation.");
        }
        return size;
    }

    @Override
    public float getRotationSpeed() {
        return -40f;
    }
    public float getCoreRotationRatio(){
        return 1f;
    }

    @Override
    public void render(T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = fluidList.getOrDefault(entity.getBlockPos(), FluidStack.EMPTY);

        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        // デルタ時間を計算（秒単位）
        float deltaTime = (currentFrameTime - entity.getLastFrameTime()) / 1_000_000_000.0F;
        //オーブサイズをコアのサイズに初期化
        float orbSize = getMinOrbSize();

        //渡すデータをつくる
        HashMap<String,Object> renderingRequires = new HashMap<>();

        //タンクが空なら液体の描画処理をスキップ
        if (fluidStack != null && !fluidStack.isEmpty()) {

            renderingRequires.put("fluidStack",fluidStack);

            //内容量の見た目スムージングを計算
            entity.setSmoothedTankAmount(Math.max(0.01f,Math.lerp(entity.getSmoothedTankAmount(), fluidStack.getAmount(), Math.min(1.0f,deltaTime * 15f))));

            //タンクの割合を計算
            float fillPercentage = Math.min(1.0f,entity.getSmoothedTankAmount() / entity.getTankCapacity(0));

            //タンクの割合から、オーブのサイズを計算
            //オーブのサイズを格納
            orbSize = (float) calcSize(getMinOrbSize(),getMaxOrbSize(),fillPercentage);

            //液体を出し入れしたときに回転を加算
            entity.setRotationOffset(entity.getRotationOffset() + deltaTime * java.lang.Math.abs(entity.getSmoothedTankAmount() - fluidStack.getAmount()) * -0.25f);
        }
        else
        {
            entity.setSmoothedTankAmount(0f);
            // 現在のフレーム時間を保存
            entity.setLastFrameTime(System.nanoTime());
        }

        //回転オフセットに加算
        entity.setRotationOffset(entity.getRotationOffset() + (deltaTime / orbSize) * getRotationSpeed());
        //上下動オフセットに加算
        entity.setWaveOffset(entity.getWaveOffset() + (deltaTime / orbSize) * 170f);

        renderingRequires.put("orbSize",orbSize);
        renderingRequires.put("rotationOffset", entity.getRotationOffset() * getCoreRotationRatio());
        renderingRequires.put("waveOffset", entity.getWaveOffset());
        renderingRequires.put("combinedOffset",entity.centerOffset().add(new Vec3(0,calcOffsetY(orbSize),0)));
        renderingRequires.put("combinedLight", combinedLight);

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();

        //液体の見た目をつくるメソッドを呼び出す
        if(fluidStack!=null&&!fluidStack.isEmpty()) {
            renderFluid(poseStack, bufferSource, renderingRequires);
        }

        //コアの見た目をつくるメソッドを呼び出す
        renderCore(poseStack, bufferSource, renderingRequires);

        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();

        // 現在のフレーム時間を保存
        entity.setLastFrameTime(currentFrameTime);
    }

    //模造コアの描画
    private void renderCore(PoseStack poseStack, MultiBufferSource bufferSource, HashMap<String,Object> renderingRequires) {
        //値を取り出す
        int combinedLight = (int) renderingRequires.get("combinedLight");

        //レンダリング形式を決める
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        //コアのテクスチャを取得
        TextureAtlasSprite sprite = getCoreSprite();

        //配列に値を格納
        renderingRequires.put("matrix", poseStack.last().pose());
        renderingRequires.put("consumer", consumer);
        renderingRequires.put("sprite", sprite);
        renderingRequires.put("color", getCoreColor());
        renderingRequires.put("light",getCoreLight(combinedLight));

        //メッシュを組み立てる
        float orbSize = getUVSize();

        //配列から値を取り出す
        float rotationOffset = (float)renderingRequires.get("rotationOffset") / getCoreRotationRatio();//entity.rotationOffset;
        float waveOffset = (float)renderingRequires.get("waveOffset");//entity.waveOffset;

        float cosWaveform = CreateCosWaveform(waveOffset, orbSize *0.03f);

        //必要な座標を用意
        final float topY = orbSize * ((float) Math.sqrt(1.5))/2, sideX = orbSize * Math.sqrt(1f/3f), sideY = topY/3;
        //場合分け
        //方向1 2 3
        for (int i = 0; i < 3; i++) {
            //上面
            Vector3f[] vertPos0 = {
                    new Vector3f(0,topY + cosWaveform,0),
                    new Vector3f(CreateCosWaveform(60+i*120+rotationOffset,sideX),sideY + cosWaveform, CreateSinWaveform(60+i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(i*120+rotationOffset,sideX),-sideY + cosWaveform, CreateSinWaveform(i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(-60+i*120+rotationOffset,sideX),sideY + cosWaveform, CreateSinWaveform(-60+i*120+rotationOffset,sideX))
            };
            Vector2f[] vertUV0 = {
                    new Vector2f(16*i/3f,16),
                    new Vector2f(16*(i+1)/3f,16),
                    new Vector2f(16*(i+1)/3f,8),
                    new Vector2f(16*i/3f,8)
            };
            //メッシュを定義するメソッドを呼び出す
            //renderQuads(renderingRequires,vertPos0,vertUV0);

            //下面
            Vector3f[] vertPos1 = {
                    new Vector3f(CreateCosWaveform(60+i*120+rotationOffset,sideX),sideY + cosWaveform, CreateSinWaveform(60+i*120+rotationOffset,sideX)),
                    new Vector3f(CreateCosWaveform(120+i*120+rotationOffset,sideX),-sideY + cosWaveform, CreateSinWaveform(120+i*120+rotationOffset,sideX)),
                    new Vector3f(0,-topY + cosWaveform,0),
                    new Vector3f(CreateCosWaveform(i*120+rotationOffset,sideX),-sideY + cosWaveform, CreateSinWaveform(i*120+rotationOffset,sideX))
            };
            Vector2f[] vertUV1 = {
                    new Vector2f(16*i/3f,8),
                    new Vector2f(16*(i+1)/3f,8),
                    new Vector2f(16*(i+1)/3f,0),
                    new Vector2f(16*i/3f,0)
            };
            //メッシュを定義するメソッドを呼び出す
            //renderQuads(renderingRequires,vertPos1,vertUV1);
        }
    }

    private Object getCoreLight(int combinedLight) {
        return combinedLight;
    }

    private Object getCoreColor() {
        return new float[]{1f,1f,1f};
    }

    private TextureAtlasSprite getCoreSprite() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation("heliopause", "block/imitation_core"));
    }
}
