package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.heliopause.blockEntity.StellarIngredientBlockEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaternionf;

import java.util.HashMap;

public class StellarIngredientBlockRenderer<T extends StellarIngredientBlockEntity> implements BlockEntityRenderer<T> {
    protected static long lastFrameTime;
    protected static double currentRotation;

    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;
    private final BakedModel itemSphereModel;

    public StellarIngredientBlockRenderer(BlockEntityRendererProvider.Context context) {
        super();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.itemSphereModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_ITEM_SPHERE);
    }

    @Override
    public void render(@NotNull T entity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        BlockPos blockPos = entity.getBlockPos();
        LazyOptional<IItemHandler> itemCapability = entity.getCapability(ForgeCapabilities.ITEM_HANDLER);
        LazyOptional<IFluidHandler> fluidCapability = entity.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (!itemCapability.isPresent() || !fluidCapability.isPresent()) {
            return;
        }
        IItemHandler itemHandler = itemCapability.orElseThrow(IllegalStateException::new);
        IFluidHandler fluidHandler = fluidCapability.orElseThrow(IllegalStateException::new);

        ItemStack itemStack = itemHandler.getStackInSlot(0);
        FluidStack fluidStack = fluidHandler.getFluidInTank(0);

        //衛星のサイズを計算
        float satSize = CircumstellarIngredient.getSatRadius(new CircumstellarIngredient.StellarStack(itemStack, fluidStack));

        Vec3 blockCenter = Vec3.ZERO.add(0,calcOffsetY(satSize),0);
        Level level = entity.getLevel();

        float rotationOffset = (float) getRotationOffset(blockPos.asLong(), 2);

        if(!itemStack.isEmpty()){
            // 装飾で覆う
            renderItemSphere(blockRenderer, itemSphereModel, blockCenter, satSize, rotationOffset, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
            //レンダリング
            renderItemSatellite(blockRenderer, itemRenderer, blockCenter, satSize, rotationOffset, itemStack, level, blockPos, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        }else if(!fluidStack.isEmpty()){
            // 装飾で覆う
            renderItemSphere(blockRenderer, itemSphereModel,blockCenter, satSize, rotationOffset, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
            //レンダリング
            renderFluidSatellite(blockCenter, satSize, rotationOffset, fluidStack, partialTicks, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
    }

    public static float calcOffsetY(float orbSize) {
        return (Math.max(0,orbSize - 0.3f)*0.5f) * Math.sqrt(3);
    }

    private double getRotationOffset(long random, int divider) {
        // 現在のフレーム時間を取得
        long currentFrameTime = System.nanoTime();
        if(currentFrameTime != lastFrameTime){
            double deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0F;
            if(deltaTime > 0){
                currentRotation += (float) (deltaTime);
                currentRotation %= 360 * 2 * Math.PI; // 約数の多い回転数で切る
                lastFrameTime = currentFrameTime;
            }
        }

        double localRotation = currentRotation + RandomSource.create(random).nextInt(360)/(6 * Math.PI);

        // 時刻から回転角度を設定
        return (localRotation / divider) % (2*Math.PI);
    }

    public static void renderFluidSatellite(
        Vec3 satPos, float satSize, float satRot, FluidStack fluidStack,
        float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){

        float fluidSatSize = satSize * 1.6f;
        float satRotAngle = (float) (satRot * 180 / Math.PI);
        float itemWaveOffset = ((1 + fluidSatSize)*0.005f * Math.cos(satRot*(int)(1.0f/satSize)));
        //渡すデータをつくる
        HashMap<String,Object> renderingRequires = new HashMap<>();
        renderingRequires.put("fluidStack",fluidStack);

        renderingRequires.put("combinedLight", combinedLight);

        renderingRequires.put("orbSize", fluidSatSize);
        renderingRequires.put("rotationOffset", satRotAngle);
        renderingRequires.put("waveOffset", /*calcOffsetY(satSize) + */itemWaveOffset);
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

    public static void renderItemSatellite(
        BlockRenderDispatcher blockRenderer, ItemRenderer itemRenderer,
        Vec3 satPos, float satSize, float satRot, ItemStack itemStack,
        Level level, BlockPos blockPos, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int satCombLight, int combinedOverlay){
        //BlockGetter level = entity.getLevel();
        if(level == null){
            return;
        }
        //オフセットを使用する形に変形
        //float satSize = 0.25f;
        //float itemRotationOffset = (float) (-satRot);
        float itemWaveOffset = ((1 + satSize)*0.005f * Math.cos(satRot*(int)(1.0f/satSize)));
        poseStack.pushPose();
        poseStack.translate(satPos.x,satPos.y,satPos.z);
        //フルブロックアイテムの場合
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState itemBlockState = blockItem.getBlock().defaultBlockState();
            if(Block.isShapeFullBlock(itemBlockState.getOcclusionShape(level, blockPos))/*itemBlockState.getCollisionShape(null,null).equals(Shapes.block())*/){
                //位置調整
                poseStack.translate(0.5, 0.5, 0.5);
                //傾きの設定
                poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
                //回転
                poseStack.mulPose(new Quaternionf().rotateAxis(-satRot, 1, 1, 1));
                //スケール調整
                //float scale = 0.25f;
                poseStack.scale(satSize, satSize, satSize);
                // 中心から戻す
                poseStack.translate(-0.5,-0.5,-0.5);
                double wholeOffset = /*calcOffsetY(satSize) + */itemWaveOffset;// * Math.sqrt(3)/scale;//scale;
                poseStack.translate(wholeOffset,wholeOffset,wholeOffset);
                //描画
                blockRenderer.renderSingleBlock(itemBlockState, poseStack, bufferSource, satCombLight, combinedOverlay, ModelData.EMPTY, RenderType.translucent());
                poseStack.popPose();
                return;
            }
        }

        //位置調整
        poseStack.translate(0.5, 0.5, 0.5);
        //回転
        poseStack.mulPose(new Quaternionf().rotateY(satRot));
        //スケール調整
        float scale = 0.4f;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0,calcOffsetY(satSize) + itemWaveOffset* Math.sqrt(3)/scale,0);
        //描画
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, satCombLight, combinedOverlay, poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }

    public static void renderItemSphere(
        BlockRenderDispatcher blockRenderer, BakedModel itemSphereModel,
        Vec3 satPos, float satSize, float satRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        //float orbSize = 1.0f;
        //float itemRotationOffset = -satRot;
        float itemWaveOffset = ((1 + satSize)*0.005f * Math.cos(satRot*(int)(1.0f/satSize)));
        float sphereSize = satSize * 4f;
        poseStack.pushPose();
        poseStack.translate(satPos.x,satPos.y,satPos.z);
        //位置調整
        poseStack.translate(0.5, 0.5, 0.5);
        //傾きの設定
        poseStack.mulPose(new Quaternionf().rotateTo(1, 1, 1, 0, 1, 0));
        //回転
        poseStack.mulPose(new Quaternionf().rotateAxis(-satRot, 1, 1, 1));
        //スケール調整
        poseStack.scale(sphereSize, sphereSize, sphereSize);
        // 中心から戻す
        poseStack.translate(-0.5,-0.5,-0.5);
        double wholeOffset = /*calcOffsetY(satSize) + */itemWaveOffset;//*satSize;
        poseStack.translate(wholeOffset,wholeOffset,wholeOffset);
        blockRenderer.getModelRenderer().renderModel(
            poseStack.last(), bufferSource.getBuffer(RenderType.cutout()), null, itemSphereModel,
            1f,1f,1f, combinedLight, combinedOverlay);
        poseStack.popPose();
    }

}
