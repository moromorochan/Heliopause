package com.moromoro.heliopause.render;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.FluidCageBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Math;

public class FluidCageBlockRenderer extends AbstractFluidOrbBlockRenderer<FluidCageBlockEntity> {
    public FluidCageBlockRenderer(BlockEntityRendererProvider.Context context){
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
