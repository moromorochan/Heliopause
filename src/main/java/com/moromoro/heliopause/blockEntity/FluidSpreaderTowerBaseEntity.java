package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidSpreaderTowerBaseEntity extends AbstractFluidConcealBlockEntity {
    public FluidSpreaderTowerBaseEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_BASE_BE.get(), pos, state, 1000);
    }
}
