package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.AltAzimuthCircleBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AltAzimuthCircleBoardBlock extends AbstractWrittenBoardBlock{

    public AltAzimuthCircleBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new AltAzimuthCircleBoardBlockEntity(blockPos, blockState);
    }
}
