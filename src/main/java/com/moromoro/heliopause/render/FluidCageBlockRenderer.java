package com.moromoro.heliopause.render;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.FluidCageBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
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
    public boolean shouldRender(FluidCageBlockEntity entity, Vec3 vec3) {
        return (entity.getBlockState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) && super.shouldRender(entity, vec3);
    }

    @Override
    public float getMaxOrbSize(){
        return 1f;
    }
}
