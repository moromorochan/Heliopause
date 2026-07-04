package com.moromoro.heliopause.block;

import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class AlchemyBironBlock extends Block {
    public AlchemyBironBlock(Properties properties) {
        super(properties);
    }
    
    // 磨く
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (toolAction == ToolActions.AXE_SCRAPE) {
            return BlockRegistry.POLISHED_BIRON_BLOCK.get().defaultBlockState();
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
