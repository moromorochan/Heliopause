package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.block.FluidSpreaderTowerBlock;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FluidSpreaderTowerBlockEntity extends AbstractFluidTransferBlockEntity{
    public FluidSpreaderTowerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_TOWER_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(level!=null){
            Level lev = this.level;
            //基部のブロックを機能ブロックに指定
            for (int i = 0; i < 15; i++) {
                Block block = lev.getBlockState(this.getBlockPos().below(i)).getBlock();
                if(block.equals(BlockRegistry.FLUID_SPREADER_TOWER.get())){
                    int blockLevel = lev.getBlockState(this.getBlockPos().below(i)).getValue(FluidSpreaderTowerBlock.LEVEL);
                    if(blockLevel==0){
                        setOperationBlockPos(this.getBlockPos().below(i));
                        break;
                    }
                }
            }
        }
    }
}