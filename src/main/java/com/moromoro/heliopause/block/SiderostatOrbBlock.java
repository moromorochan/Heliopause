package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.CentralStarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SiderostatOrbBlock extends CentralStarBlock {
    public SiderostatOrbBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CentralStarBlockEntity(blockPos, blockState);
    }
}
