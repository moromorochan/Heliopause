package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrucibleBlockEntity extends AbstractFluidTankEntity {
    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CRUCIBLE_BE.get(), pos, state,2000);
    }
}
