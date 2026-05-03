package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StellarIngredientDispenserBlockEntity extends BlockEntity {
    
    private int timer;
    
    public StellarIngredientDispenserBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.INGREDIENT_DISPENSER_BE.get(), blockPos, blockState);
    }
    
    public void tick(Level level, BlockPos basePos, BlockState baseBlockState, StellarIngredientDispenserBlockEntity blockEntity){
        // 出力するスペースがあるか確認
        
        // 接続するブロックからアイテムを取り出す
        
        // 隣接するブロックから液体を取り出す
        
        // 汎用材料に変換
    }
}
