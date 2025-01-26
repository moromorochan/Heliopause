package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.render.CrucibleBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrucibleBlockEntity extends AbstractFluidTankEntity {
    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CRUCIBLE_BE.get(), pos, state,1000);
    }

    @Override
    protected void updateRenderData() {
        CrucibleBlockRenderer.updateData(this.getBlockPos(), this.tank.getFluid());
    }
    @Override
    protected void removeRenderData() {
        CrucibleBlockRenderer.removeData(this.getBlockPos());
    }
}
