package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.block.FluidSpreaderTowerBlock;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FluidSpreaderTowerPoleEntity extends AbstractFluidTransferBlockEntity{
    public FluidSpreaderTowerPoleEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_POLE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(level!=null){
            Level level = this.level;
            //基部のブロックを機能ブロックに指定
            for (int i = 0; i < 15; i++) {
                BlockPos belowPos = this.getBlockPos().below(i);
                BlockState belowBlockState = level.getBlockState(belowPos);
                if(belowBlockState.is(BlockRegistry.FLUID_SPREADER_TOWER.get())){
                    int blockLevel = belowBlockState.getValue(FluidSpreaderTowerBlock.LEVEL);
                    if(blockLevel==0){
                        setOperationBlockPos(belowPos);
                        //Heliopause.LOGGER.debug(String.format("blockPos: %s, operatorPos: %s",this.getBlockPos().toString(),belowPos.toString()));
                        break;
                    }
                }
            }
        }
    }
}