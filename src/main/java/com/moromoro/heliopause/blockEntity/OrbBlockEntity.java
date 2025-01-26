package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.render.OrbBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class OrbBlockEntity extends AbstractFluidOrbBlockEntity {
    public OrbBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ORB_BE.get(), pos, state,8000);
    }
    @Override
    protected void updateRenderData() {
        OrbBlockRenderer.updateData(this.getBlockPos(), this.tank.getFluid());
    }
    @Override
    protected void removeRenderData() {
        OrbBlockRenderer.removeData(this.getBlockPos());
    }
}