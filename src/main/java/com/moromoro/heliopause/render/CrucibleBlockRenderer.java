package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.CrucibleBlock;
import com.moromoro.heliopause.blockEntity.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;

import java.util.HashMap;

public class CrucibleBlockRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    public CrucibleBlockRenderer(BlockEntityRendererProvider.Context context){
    }
    //ブロックの端から液面の端までの距離
    private static final float MARGIN = 2f/16;//(float) (CrucibleBlock.INNER.min(Direction.Axis.X) /16f);
    private static final float FLUID_TOP = 13f;
    private static final float FLUID_BOTTOM = 2f;//(float) (CrucibleBlock.INNER.min(Direction.Axis.Y));

    private static final HashMap<BlockPos,FluidStack> fluidList = new HashMap<>();

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
    public void render(CrucibleBlockEntity entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        FluidStack fluidStack = fluidList.getOrDefault(entity.getBlockPos(), FluidStack.EMPTY);
        //タンクが空なら描画処理を完了
        if (fluidStack.isEmpty())
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
        entity.setSmoothedTankAmount(Math.lerp(entity.getSmoothedTankAmount(), fluidStack.getAmount(), deltaTime * 15f));
        //entity.setSmoothedTankAmount(Math.clamp(0, entity.getTankCapacity(0), entity.getSmoothedTankAmount()));

        //液面高さの上限と下限を決める
        //final float fillMax = 15f, fillMin = 5f;
        //タンクの割合から液面高さを計算
        float fillPercentage = Math.clamp(FLUID_BOTTOM, FLUID_TOP, FLUID_BOTTOM + (FLUID_TOP-FLUID_BOTTOM)*(entity.getSmoothedTankAmount() / entity.getTankCapacity(0)))/16f;

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        //液体の見た目をつくるメソッドを呼び出す
        renderFluid(poseStack, bufferSource, fluidStack, fillPercentage, combinedLight);
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();

        // 現在のフレーム時間を保存
        entity.setLastFrameTime(currentFrameTime);
    }

    private static void renderFluid(PoseStack poseStack, MultiBufferSource bufferSource, FluidStack fluidStack, float heightPercentage, int combinedLight) {
        //レンダリング形式を決める
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        //(とどまる)液体テクスチャの取得
        TextureAtlasSprite sprite = getFluidSprite(fluidStack);
        //液体のtintカラーの取得
        float[] color = getFluidColor(fluidStack);

        //ブロックの光レベルの取得
        int light = calcLight(combinedLight,fluidStack);
        //メッシュを定義するメソッドを呼び出す
        renderQuads(poseStack.last().pose(), consumer, sprite, color, heightPercentage, light);
    }

    //(とどまる)液体テクスチャの取得
    private static TextureAtlasSprite getFluidSprite(FluidStack fluidStack){
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
    private static float[] getFluidColor(FluidStack fluidStack) {
        int color = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor();
        return new float[] {
                (float) FastColor.ARGB32.red(color)/255f,
                (float) FastColor.ARGB32.green(color)/255f,
                (float) FastColor.ARGB32.blue(color)/255f
        };
    }

    //ブロックの光レベルの取得
    private static int calcLight(int combinedLight, FluidStack fluidStack){
        int skyLight = combinedLight >> 20 & 15;
        int blockLight = combinedLight >> 4 & 15;
        //液体の明るさの取得
        int fluidLight = fluidStack.getFluid().getFluidType().getLightLevel();
        //計算
        int maxBlockLight =Math.max(blockLight, fluidLight);
        return (skyLight << 20| maxBlockLight << 4);
    }

    private static void renderQuads(Matrix4f matrix, VertexConsumer buffer, TextureAtlasSprite sprite, float[] color, float heightPercentage, int light) {

        //色を取り出す
        float r = color[0];
        float g = color[1];
        float b = color[2];

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
