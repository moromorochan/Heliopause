package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractCoreBlockEntity extends AbstractFluidOrbBlockEntity {
    public AbstractCoreBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state, capacity);
    }
}