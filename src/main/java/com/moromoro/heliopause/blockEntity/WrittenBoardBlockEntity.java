package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WrittenBoardBlockEntity extends BlockEntity {
    public WrittenBoardBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.WRITTEN_BOARD_BE.get(), pos, blockState);
    }
}
