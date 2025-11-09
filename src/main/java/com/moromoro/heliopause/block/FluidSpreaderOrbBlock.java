package com.moromoro.heliopause.block;

import com.moromoro.heliopause.blockEntity.FluidSpreaderOrbBlockEntity;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class FluidSpreaderOrbBlock extends BaseEntityBlock {
    public FluidSpreaderOrbBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidSpreaderOrbBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(),
                (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
        );
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving) {
        //中心星アイテムをドロップ
        if(!blockState.is(newBlockState.getBlock())){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof FluidSpreaderOrbBlockEntity orbBlockEntity){
                ItemEntity itemEntity = new ItemEntity(level, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, orbBlockEntity.getCenterBlockState().getBlock().asItem().getDefaultInstance());
                level.addFreshEntity(itemEntity);
            }
        }

        super.onRemove(blockState, level, pos, newBlockState, isMoving);
    }
}
