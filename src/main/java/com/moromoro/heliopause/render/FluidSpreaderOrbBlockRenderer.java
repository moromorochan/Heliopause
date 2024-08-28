package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FluidSpreaderOrbBlockRenderer<T extends FluidSpreaderOrbBlockEntity> extends AbstractFluidOrbBlockRenderer<T>{

    public FluidSpreaderOrbBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void putProperties(T entity, float deltaTime, HashMap<String, Object> renderingRequires) {
        super.putProperties(entity, deltaTime, renderingRequires);

        FluidStack fluidStack = (FluidStack) renderingRequires.get("fluidStack");
        //回転オフセットに加算
        entity.setRingRotationOffset(entity.getRotationOffset() + (deltaTime / getRingRadius()));
        renderingRequires.put("ringRot",entity.getRingRotationOffset());

        //0-1か1-0のときの変化量を取得
        float density = entity.getSmoothedRingDensity();
        if(!fluidStack.isEmpty() && density<1)//液体が入れられたが環がまだ完全でないとき
        {
            entity.setSmoothedRingDensity(density+(0.08f*deltaTime));
        } else if (fluidStack.isEmpty() && density>0) //液体が無くなったが環がまだあるとき
        {
            entity.setSmoothedRingDensity(density-(0.08f*deltaTime));
        }
        renderingRequires.put("density",entity.getSmoothedRingDensity());
    }

    @Override
    protected boolean permanentRender(T entity) {
        return (entity.getSmoothedTankAmount()==0 && entity.getSmoothedRingDensity()==0);
    }

    @Override
    public float getMaxOrbSize() {
        return 1f;
    }

    @Override
    public void renderGroup(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, HashMap<String, Object> renderingRequires) {
        super.renderGroup(poseStack, bufferSource, renderingRequires);
        renderRing(poseStack,bufferSource,renderingRequires);
    }
    private void renderRing(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, HashMap<String, Object> renderingRequires){
        //液体テクスチャを取得する
        FluidStack fluidStack = (FluidStack) renderingRequires.get("fluidStack");
        TextureAtlasSprite stillSprite= getFluidStillSprite(fluidStack);
        renderingRequires.put("stillSprite",stillSprite);

        //ピクセルの配置位置を格納するリストを用意
        List<Vector2i> pixelList = new ArrayList<>();

        //現時点での密度を取得
        float density = (float) renderingRequires.get("density");

        //環の幅
        float width = getRingWidth() * density;
        //環の平均(幅の中央)半径
        float averageRadius = getRingRadius() - (width*(1-density));

        //環が大きくなれば荒くなり、幅が狭ければ少し細かくなる。半径20で最小、半径3で最大、幅0.2で1.2倍、幅3で0.8倍
        float resolutionOffset = Math.clamp(0,1, 1-(averageRadius-1.4f)*(18.6f/20))* (1.2f - (width / 3) * 0.4f);

        int resolution = 4;//解像度、一ブロック毎のピクセル数

        for (float h = 0; h < width; h+= 0.1f/resolution) {

            //端だけ細かく、内側は粗く
            if(h==0){
                resolution = 2+Math.round(Math.lerp(0,14, resolutionOffset));
                h+= (1f-density)/16;
            }
            else if(h<(width*0.25f)){
                resolution = 2+Math.round(Math.lerp(0,14, resolutionOffset));
                h+= (1f-density)/8;
            } else if (h<(width*0.35f)&& width>(0.5f/resolution)) {
                resolution = 1+Math.round(Math.lerp(0,7, resolutionOffset));
                h+= (1f-density)/4;
            } else if (h<(width * 0.65f)&& width>(0.3f/resolution)) {
                resolution = 1+Math.round(Math.lerp(0,3, resolutionOffset));
            }else if (h<(width * 0.75f)&& width>(0.5f/resolution)) {
                resolution = 1+Math.round(Math.lerp(0,7, resolutionOffset));
                h+= (1f-density)/4;
            } else {
                resolution = 3+Math.round(Math.lerp(0,13, resolutionOffset));
                h+= (1f-density)/4;
            }
            renderingRequires.put("resolution", resolution);

            //環の半径
            float radius = averageRadius - (width/2f) + h;
            float angleStep = (float) (Math.atan2(1f / resolution, radius));
            int stepCount = (int)Math.ceil(2*Math.PI/(angleStep*8));
            //ドットの配置
            for (int i = 0; i < stepCount; i++) {
                float angle = i * angleStep;
                int posX = (int)Math.round(radius * resolution * Math.cos(angle));
                int posY = (int)Math.round(radius * resolution * Math.sin(angle));
                Vector2i[] pixPos = {
                        new Vector2i(posX,posY),//
                        new Vector2i(posX,-posY),//
                        new Vector2i(posY,-posX),//
                        new Vector2i(-posY,-posX),//
                        new Vector2i(-posX,-posY),//
                        new Vector2i(-posX,posY),//
                        new Vector2i(-posY,posX),//
                        new Vector2i(posY,posX),//
                };

                //色を設定
                int gradient = Math.min(255,191 + 4*Math.round(64*h/(width*2)));
                int color = FastColor.ARGB32.multiply(
                        FastColor.ARGB32.color(255,gradient,gradient,gradient),
                        IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor()
                );
                    renderingRequires.put("color",color);

                    //描画 それぞれのピクセルを対象になるように配置
                    for (int j = 0; j < 8; j++) {
                        //同じ位置にドットが二つ重ならないように監視
                        if(!pixelList.contains(pixPos[j])){
                        renderingRequires.put("position", pixPos[j]);
                            //if(RandomSource.create().nextFloat()>density){continue;}

                        //スプライト内のuv位置を計算
                        int ringU =Math.round(h*16);
                        int ringVOffset = (int) Math.round((float) renderingRequires.get("ringRot")*4 *resolution/(32*radius*Math.PI));
                        int ringV = j %2 !=0 ? i : -i;
                        Vector2i uvPos = new Vector2i((ringU)%8+8, (ringV+ringVOffset)%8+8);
                        renderingRequires.put("uv",uvPos);
                        drawPixel(poseStack,bufferSource,renderingRequires);
                        pixelList.add(pixPos[j]);
                    }
                }
            }
        }
    }

    private float getRingWidth() {
        return 1.5f;
    }

    private float getRingRadius() {
        return 2.0f;
    }

    private void drawPixel(PoseStack poseStack, MultiBufferSource bufferSource, HashMap<String, Object> renderingRequires) {

        Matrix4f matrix=(Matrix4f) renderingRequires.get("matrix");
        VertexConsumer buffer= bufferSource.getBuffer(RenderType.solid());//(VertexConsumer) renderingRequires.get("consumer");

        int light = (int) renderingRequires.get("light");
        TextureAtlasSprite sprite=(TextureAtlasSprite) renderingRequires.get("stillSprite");
        Vector2i uvPos = (Vector2i) renderingRequires.get("uv");

        int resolution = (int) renderingRequires.get("resolution");
        float uvSize = 4f/resolution;
        float dotSize = 1f/(resolution * 2f);

        Vector2i position = (Vector2i) renderingRequires.get("position");
        float posX = (float) position.x / resolution ;
        float posY = 0.5f;//(float) CreateCosWaveform((float)renderingRequires.get("waveOffset")+25f -uvPos.x * 160f /resolution,(float)renderingRequires.get("orbSize") *0.03f);
        float posZ = (float) position.y / resolution ;

        int color = (int) renderingRequires.get("color");
        int alpha = FastColor.ARGB32.alpha(color);
        int red = FastColor.ARGB32.red(color);
        int green = FastColor.ARGB32.green(color);
        int blue = FastColor.ARGB32.blue(color);

        //上面
        buffer.vertex(matrix, -posX - dotSize+0.5f, posY + resolution*0.001f, -posZ - dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x),sprite.getV(uvPos.y)).uv2(light).normal(0,1,0).endVertex();
        buffer.vertex(matrix, -posX - dotSize+0.5f, posY + resolution*0.001f, -posZ + dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x),sprite.getV(uvPos.y+uvSize)).uv2(light).normal(0,1,0).endVertex();
        buffer.vertex(matrix, -posX + dotSize+0.5f, posY + resolution*0.001f, -posZ + dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x+uvSize),sprite.getV(uvPos.y+uvSize)).uv2(light).normal(0,1,0).endVertex();
        buffer.vertex(matrix, -posX + dotSize+0.5f, posY + resolution*0.001f, -posZ - dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x+uvSize),sprite.getV(uvPos.y)).uv2(light).normal(0,1,0).endVertex();

        //下面
        buffer.vertex(matrix, posX - dotSize+0.5f, posY - resolution*0.001f, posZ - dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x),sprite.getV(uvPos.y)).uv2(light).normal(0,1,0).endVertex();
        buffer.vertex(matrix, posX + dotSize+0.5f, posY - resolution*0.001f, posZ - dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x+uvSize),sprite.getV(uvPos.y)).uv2(light).normal(0,1,0).endVertex();
        buffer.vertex(matrix, posX + dotSize+0.5f, posY - resolution*0.001f, posZ + dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x+uvSize),sprite.getV(uvPos.y+uvSize)).uv2(light).normal(0,1,0).endVertex();
        buffer.vertex(matrix, posX - dotSize+0.5f, posY - resolution*0.001f, posZ + dotSize+0.5f)
                .color(red,green,blue,alpha).uv(sprite.getU(uvPos.x),sprite.getV(uvPos.y+uvSize)).uv2(light).normal(0,1,0).endVertex();
    }

    //(とどまる)液体テクスチャの取得
    private static TextureAtlasSprite getFluidStillSprite(@NotNull FluidStack fluidStack){
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
}
