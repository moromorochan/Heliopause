package com.moromoro.heliopause.render;

import com.moromoro.heliopause.blockEntity.FluidCageBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Math;

public class FluidCageBlockRenderer extends AbstractFluidOrbBlockRenderer<FluidCageBlockEntity> {
    public FluidCageBlockRenderer(BlockEntityRendererProvider.Context context){
        super(context);
    }

    @Override
    public float calcSize(float sizeMax,float fillPercentage) {
        final float power = 6f;
        return (sizeMax * ((float)java.lang.Math.pow(Math.clamp(0,1,fillPercentage),1f/power)));
    }
    @Override
    public float calcOffsetY(float orbSize) {
        return 0f;
    }
    @Override
    public float getMaxOrbSize(){
        return 1f;
    }
}
