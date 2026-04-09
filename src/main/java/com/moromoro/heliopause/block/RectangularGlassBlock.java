package com.moromoro.heliopause.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RectangularGlassBlock extends RectangularBlock{
    public RectangularGlassBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean skipRendering(@NotNull BlockState blockState, @NotNull BlockState neighborState, @NotNull Direction direction) {
        //return HalfTransparentBlock.skipRendering(blockState, neighborState, direction);
        return neighborState.is(this) || super.skipRendering(blockState, neighborState, direction);
    }
    
    @Override
    public float getShadeBrightness(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos) {
        //return AbstractGlassBlock.getShadeBrightness(blockState, blockGetter, blockPos);
        return 1.0f;
    }
    
    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos) {
        //return AbstractGlassBlock.propagatesSkylightDown(blockState, blockGetter, blockPos);
        return true;
    }
}
