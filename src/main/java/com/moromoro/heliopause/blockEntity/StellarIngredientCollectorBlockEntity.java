package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StellarIngredientCollectorBlockEntity extends BlockEntity {
    
    private int timer;
    
    public StellarIngredientCollectorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.INGREDIENT_COLLECTOR_BE.get(), blockPos, blockState);
    }
    
    public void tick(Level level, BlockPos basePos, BlockState baseBlockState, StellarIngredientCollectorBlockEntity blockEntity){
    
    }
}
