package com.moromoro.heliopause.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.heliopause.entity.StellarIngredientEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.CustomModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

public class StellarIngredientEntityRenderer extends EntityRenderer<StellarIngredientEntity> {
    protected static long lastFrameTime;
    protected static double currentRotation;

    private final BlockRenderDispatcher blockRenderer;
    private final ItemRenderer itemRenderer;
    private final BakedModel itemSphereModel;

    public StellarIngredientEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.itemSphereModel = Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.ORRERY_ITEM_SPHERE);
    }

    @Override
    public ResourceLocation getTextureLocation(StellarIngredientEntity entity) {
        return null;
    }

    @Override
    public void render(StellarIngredientEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight) {

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

        Vec3 blockCenter = Vec3.ZERO.add(-0.5,-0.5 + entity.getBbHeight() * 0.5,-0.5).add(entity.getDeltaMovement().scale(partialTicks));//entity.getPosition(partialTicks);//
        Level level = entity.level();

        float rotationOffset = (float) getRotationOffset(entity.getUUID().getLeastSignificantBits(), 2);

        if(!itemStack.isEmpty()){
            // 装飾で覆う
            StellarIngredientBlockRenderer.renderItemSphere(blockRenderer, itemSphereModel, blockCenter, satSize, rotationOffset, partialTicks, poseStack, bufferSource, combinedLight, 0);
            //レンダリング
            StellarIngredientBlockRenderer.renderItemSatellite(blockRenderer, itemRenderer, blockCenter, satSize, rotationOffset, itemStack, level, BlockPos.containing(blockCenter), partialTicks, poseStack, bufferSource, combinedLight, 0);
        }else if(!fluidStack.isEmpty()){
            // 装飾で覆う
            StellarIngredientBlockRenderer.renderItemSphere(blockRenderer, itemSphereModel,blockCenter, satSize, rotationOffset, partialTicks, poseStack, bufferSource, combinedLight, 0);
            //レンダリング
            StellarIngredientBlockRenderer.renderFluidSatellite(blockCenter, satSize, rotationOffset, fluidStack, partialTicks, poseStack, bufferSource, combinedLight, 0);
        }
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
}
