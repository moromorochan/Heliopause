package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FluidSpreaderOrbBlockRenderer<T extends FluidSpreaderOrbBlockEntity> implements BlockEntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderer;

    public FluidSpreaderOrbBlockRenderer(BlockEntityRendererProvider.Context context) {
        super();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }
    protected static HashMap<BlockPos,List<CircumstellarIngredient>> ingredientsList = new HashMap<>();

    public static void updateData(BlockPos pos, List<CircumstellarIngredient> ingredientList) {
        if(ingredientList.isEmpty()){removeData(pos); return;}

        if (ingredientsList.containsKey(pos)){
            ingredientsList.replace(pos,ingredientList);
        }else{
            ingredientsList.put(pos,ingredientList);
        }
    }
    public static void removeData(BlockPos pos){
        ingredientsList.remove(pos);
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
        /*if(partialTicks<=0.1f){
            entity.requestModelDataUpdate();
        }*/
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
            //中心星の描画
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

            //衛星の描画
            List<CircumstellarIngredient> ingredients = ingredientsList.getOrDefault(entity.getBlockPos(), new ArrayList<>());//entity.getCircumstellars();
            for (CircumstellarIngredient ingredient : ingredients) {
                if(!ingredient.disk_shaped/*shouldRenderSatellite(entity, ingredient,partialTicks)*/){
                    //Heliopause.LOGGER.debug("shouldRenderSatellite passed");
                    if(!ingredient.getFluidStack().isEmpty()){
                        //衛星の位置を取得
                        Vec3 satPos = getSatPos(entity, ingredient, partialTicks);
                        //衛星のサイズを計算
                        float satSize = getSatRadius(entity,ingredient);
                        //自転オフセットを公転から用意する
                        float satRot = (ingredient.getRotationRatio() * ingredient.getRevolutionOffset())%360;
                        //衛星の位置から光の影響を取得
                        int satCombLight = getCombinedLight((ClientLevel) entity.getLevel(), satPos);
                        //レンダリング
                        renderFluidSatellite(satPos, satSize, satRot, ingredient.getFluidStack(),entity, partialTicks, poseStack, bufferSource, satCombLight, combinedOverlay);
                    }
                }
            }
            // 現在のフレーム時間を保存
            entity.setLastFrameTime(currentFrameTime);
        }
    }

    //衛星の位置(原点=ブロックエンティティ)を取得
    private Vec3 getSatPos(FluidSpreaderOrbBlockEntity entity, CircumstellarIngredient ingredient, float partialTicks){
        //極座標を取得
        float radius = ingredient.getOrbitalRadius();
        float revolution = ingredient.getRevolutionOffset();
        //前回tick時点のrevolutionを計算
        float prevRevolution = revolution - (Mth.sqrt(entity.getCentForce() / radius) / radius);
        //滑らかな巡行を計算
        float smoothRevolution = Math.lerp(prevRevolution, revolution, partialTicks);
        //ラジアンへ変換
        float revolutionRadians = Math.toRadians(smoothRevolution);
        //極座標から直交座標へ変換
        return new Vec3(radius * Math.sin(revolutionRadians), 0, radius * Math.cos(revolutionRadians));
    }

    //衛星のサイズを計算
    private float getSatRadius(FluidSpreaderOrbBlockEntity entity, CircumstellarIngredient ingredient){
        float fillPercentage =  ingredient.getFluidStack().getAmount() /(float) entity.getTankCapacity(0);
        return fillPercentage * fillPercentage * fillPercentage * 0.7f;
    }

    //衛星がそれぞれ画面内にあるかの判定
    protected boolean shouldRenderSatellite(FluidSpreaderOrbBlockEntity entity, CircumstellarIngredient ingredient, float partialTicks){
        //形状を確認 円盤ならスキップ
        if(!ingredient.getDisk_shaped()) {
            //衛星の位置を取得
            Vec3 satPos = getSatPos(entity, ingredient, partialTicks).add(entity.getBlockPos().getCenter());
            //衛星のサイズを計算
            float satSize = getSatRadius(entity,ingredient);
            //値から衛星のバウンディングボックスを決定
            AABB ingredientBoundingBox = new AABB(satPos, satPos).inflate(satSize);
            // バウンディングボックスが画面内にあるかどうかを判定
            if (Minecraft.getInstance().cameraEntity != null) {
                return Minecraft.getInstance().cameraEntity.getBoundingBox().intersects(ingredientBoundingBox);
            }
        }
        return false;
    }
    //ブロック自体が画面外でも、衛星に描画が必要な場合の判定
    protected boolean shouldRenderListSatellites(FluidSpreaderOrbBlockEntity entity, float partialTicks){
        List<CircumstellarIngredient> ingredients = entity.getCircumstellars();
        for (CircumstellarIngredient ingredient : ingredients) {
            if(shouldRenderSatellite(entity, ingredient, partialTicks)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(T entity) {
        return shouldRenderListSatellites(entity, 0);
    }

    private void renderFluidSatellite(
        Vec3 satPos, float satSize, float satRot, FluidStack fluidStack,
        @NotNull FluidSpreaderOrbBlockEntity entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){

        //渡すデータをつくる
        HashMap<String,Object> renderingRequires = new HashMap<>();
        renderingRequires.put("fluidStack",fluidStack);

        renderingRequires.put("combinedLight", combinedLight);

        renderingRequires.put("orbSize",satSize);
        renderingRequires.put("rotationOffset", satRot);
        renderingRequires.put("waveOffset", 0f);
        renderingRequires.put("combinedOffset",satPos);

        //親モデルをスタックに保管して、子モデルの編集をはじめる
        poseStack.pushPose();
        // 中心をブロックの中心に合わせる
        //poseStack.translate(0.5, 0.5, 0.5);
        //液体の見た目をつくるメソッドを呼び出す
        AbstractFluidOrbBlockRenderer.renderFluid(poseStack,bufferSource,renderingRequires);
        //親モデルをスタックから取り出して、子モデルの編集をおわる
        poseStack.popPose();
    }

    //任意の位置の明るさを取得
    private int getCombinedLight(ClientLevel level, Vec3 pos){
        BlockPos blockpos = BlockPos.containing(pos.x, pos.y, pos.z);
        return level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
    }

    public float getRotationSpeed() {
        return -40f;
    }
}
