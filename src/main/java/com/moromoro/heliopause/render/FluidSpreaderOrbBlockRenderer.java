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
    public double calcSize(double sizeMin ,double sizeMax,double fillRatio) {
        float power = 6f;
        double size = (sizeMin + (sizeMax-sizeMin) * (java.lang.Math.pow(Math.clamp(0,1,fillRatio),1f/power)));
        if(size == 0){
            Heliopause.LOGGER.error("Rendering failure on orb size calculation.");
        }
        return size;
    }

    @Override
    public float getMaxOrbSize(){
        return 1f;
    }
}
