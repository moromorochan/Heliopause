package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.CometCoreBlockEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CometCoreBlock extends AbstractFluidTankBlock{
    public CometCoreBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CometCoreBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        if(blockState.getBlock() != newBlockState.getBlock()){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CometCoreBlockEntity){
                ((CometCoreBlockEntity)blockEntity).setRemoved();
            }
        }
        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.COMET_CORE_BE.get(),
                (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
        );
    }
}
