package com.moromoro.heliopause.block;

import com.moromoro.heliopause.worldgen.tree.PaperBushTreeGrower;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PaperBushPlantBlock extends AzaleaBlock {
    private static final PaperBushTreeGrower TREE_GROWER = new PaperBushTreeGrower();

    public PaperBushPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos pos, BlockState state) {
        TREE_GROWER.growTree(serverLevel, serverLevel.getChunkSource().getGenerator(), pos, state, randomSource);
    }
}
