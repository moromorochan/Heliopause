package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidSpreaderOrbBlock extends AbstractFluidTankBlock{
    public FluidSpreaderOrbBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidSpreaderOrbBlockEntity(pos, state);
    }
}
