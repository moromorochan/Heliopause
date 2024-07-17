package com.moromoro.heliopause.block;

import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class CopperPipeBlock extends AbstractNetworkBlock{

    public CopperPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        //diameterを取得
        int diameter = state.getValue(CENTER);

        return switch (diameter) {
            case 1 -> new ItemStack(ItemRegistry.LOW_COPPER_PIPE_ITEM.get());
            case 2 -> new ItemStack(ItemRegistry.MEDIUM_COPPER_PIPE_ITEM.get());
            case 3 -> new ItemStack(ItemRegistry.HIGH_COPPER_PIPE_ITEM.get());
            default -> ItemStack.EMPTY;
        };
    }
}
